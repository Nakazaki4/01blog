import { marked } from 'marked';
import DOMPurify from 'dompurify';

const isBrowser = typeof window !== 'undefined' && typeof document !== 'undefined';

export function renderMarkdown(markdown: string): string {
  const html = marked.parse(markdown ?? '', { async: false }) as string;
  return isBrowser ? DOMPurify.sanitize(html) : '';
}

export function stripMarkdown(markdown: string): string {
  return (markdown ?? '')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/[`*_>#~]/g, '')
    .replace(/\s+/g, ' ')
    .trim();
}
