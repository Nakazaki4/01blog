import {
  Component,
  computed,
  ElementRef,
  inject,
  input,
  OnInit,
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

@Component({
  selector: 'app-post-create',
  imports: [FormsModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './post-create.html',
  styleUrl: './post-create.css',
})
export class PostCreateComponent implements OnInit {
  private postService = inject(PostSnippetService);

  editPostId = input<number | null>(null);
  initialBody = input<string>('');

  postCreated = output<PostResponse>();
  postUpdated = output<PostResponse>();

  textarea = viewChild.required<ElementRef<HTMLTextAreaElement>>('editor');
  fileInput = viewChild.required<ElementRef<HTMLInputElement>>('fileInput');

  body = signal('');
  uploading = signal(false);
  submitting = signal(false);
  error = signal<string | null>(null);

  isEditing = computed(() => this.editPostId() != null);

  ngOnInit(): void {
    const initial = this.initialBody();
    if (initial) this.body.set(initial);
  }

  readonly minChars = MIN_CHARS;
  readonly maxChars = MAX_CHARS;
  charCount = computed(() => this.body().length);
  tooShort = computed(() => this.charCount() < MIN_CHARS);
  tooLong = computed(() => this.charCount() > MAX_CHARS);
  outOfRange = computed(() => this.tooShort() || this.tooLong());

  onPickImage(): void {
    if (this.uploading() || this.submitting()) return;
    this.fileInput().nativeElement.click();
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

    const editId = this.editPostId();
    const call = editId != null
      ? this.postService.update(editId, { description })
      : this.postService.create({ description });

    call.subscribe({
      next: (post) => {
        this.submitting.set(false);
        if (editId != null) {
          this.postUpdated.emit(post);
        } else {
          this.body.set('');
          this.postCreated.emit(post);
        }
      },
      error: (err) => {
        this.error.set(
          err.error?.message ||
            (editId != null ? 'Could not update post' : 'Could not create post'),
        );
        this.submitting.set(false);
      },
    });
  }
}
