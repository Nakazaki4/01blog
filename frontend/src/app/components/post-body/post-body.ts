import { Component, computed, input, ViewEncapsulation } from '@angular/core';
import { renderMarkdown } from '../../shared/markdown';

@Component({
  selector: 'app-post-body',
  template: `<div class="post-body" [innerHTML]="html()"></div>`,
  styleUrl: './post-body.css',
  encapsulation: ViewEncapsulation.None,
})
export class PostBodyComponent {
  markdown = input.required<string>();
  html = computed(() => renderMarkdown(this.markdown()));
}
