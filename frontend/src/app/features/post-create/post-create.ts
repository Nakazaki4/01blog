import {
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  PostSnippetService,
} from '../../components/post-snippet/post-snippet.service';
import { PostResponse } from '../../components/post-snippet/post-snippet';

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
const MAX_SIZE_BYTES = 10 * 1024 * 1024;
const MIN_CHARS = 2000;
const MAX_CHARS = 10000;
const IMAGE_MARKDOWN = /!\[[^\]]*\]\(([^)]+)\)/g;

@Component({
  selector: 'app-post-create',
  imports: [FormsModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './post-create.html',
  styleUrl: './post-create.css',
})
export class PostCreateComponent {
  private postService = inject(PostSnippetService);

  postId = input<number | null>(null);
  initialDescription = input<string>('');

  postSaved = output<PostResponse>();

  textarea = viewChild.required<ElementRef<HTMLTextAreaElement>>('editor');
  fileInput = viewChild.required<ElementRef<HTMLInputElement>>('fileInput');

  body = signal('');
  uploading = signal(false);
  submitting = signal(false);
  error = signal<string | null>(null);

  readonly minChars = MIN_CHARS;
  readonly maxChars = MAX_CHARS;
  charCount = computed(() => this.body().length);
  tooShort = computed(() => this.charCount() < MIN_CHARS);
  tooLong = computed(() => this.charCount() > MAX_CHARS);
  outOfRange = computed(() => this.tooShort() || this.tooLong());
  isEditMode = computed(() => this.postId() !== null);
  currentImages = computed(() => {
    const urls: string[] = [];
    const re = new RegExp(IMAGE_MARKDOWN);
    let match: RegExpExecArray | null;
    while ((match = re.exec(this.body())) !== null) {
      urls.push(match[1]);
    }
    return urls;
  });

  constructor() {
    effect(() => {
      const initial = this.initialDescription();
      if (initial) {
        this.body.set(initial);
      }
    });
  }

  onPickImage(): void {
    if (this.uploading() || this.submitting()) return;
    this.fileInput().nativeElement.click();
  }

  removeImage(url: string): void {
    if (this.submitting()) return;
    const escaped = url.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const pattern = new RegExp(`\\n?!\\[[^\\]]*\\]\\(${escaped}\\)\\n?`, 'g');
    this.body.set(this.body().replace(pattern, '\n'));
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;

    if (!ALLOWED_TYPES.includes(file.type)) {
      this.error.set('Only JPEG, PNG, WebP, or GIF images are allowed.');
      return;
    }
    if (file.size > MAX_SIZE_BYTES) {
      this.error.set('Image exceeds the 10MB limit.');
      return;
    }

    this.error.set(null);
    this.uploading.set(true);

    const formData = new FormData();
    formData.append('image', file);

    this.postService.uploadImage(formData).subscribe({
      next: ({ url }) => {
        this.insertAtCursor(`\n![image](${url})\n`);
        this.uploading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Image upload failed');
        this.uploading.set(false);
      },
    });
  }

  private insertAtCursor(text: string): void {
    const el = this.textarea().nativeElement;
    const start = el.selectionStart ?? el.value.length;
    const end = el.selectionEnd ?? el.value.length;
    const next = el.value.slice(0, start) + text + el.value.slice(end);
    this.body.set(next);

    queueMicrotask(() => {
      el.focus();
      const caret = start + text.length;
      el.setSelectionRange(caret, caret);
    });
  }

  submit(): void {
    const description = this.body().trim();
    if (!description) {
      this.error.set('Write something before posting.');
      return;
    }
    if (description.length < MIN_CHARS) {
      this.error.set(`At least ${MIN_CHARS} characters required.`);
      return;
    }
    if (description.length > MAX_CHARS) {
      this.error.set(`At most ${MAX_CHARS} characters allowed.`);
      return;
    }
    if (this.uploading() || this.submitting()) return;

    this.error.set(null);
    this.submitting.set(true);

    const id = this.postId();
    const call = id !== null
      ? this.postService.update(id, { description })
      : this.postService.create({ description });

    call.subscribe({
      next: (post) => {
        if (id === null) this.body.set('');
        this.submitting.set(false);
        this.postSaved.emit(post);
      },
      error: (err) => {
        this.error.set(
          err.error?.message || (id !== null ? 'Could not update post' : 'Could not create post'),
        );
        this.submitting.set(false);
      },
    });
  }
}
