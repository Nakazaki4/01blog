import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly KEY = 'theme';

  isDark = signal(false);

  constructor() {
    if (!this.isBrowser) return;
    const saved = localStorage.getItem(this.KEY);
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.setDark(saved ? saved === 'dark' : prefersDark);
  }

  toggle(): void {
    this.setDark(!this.isDark());
  }

  private setDark(dark: boolean): void {
    this.isDark.set(dark);
    if (this.isBrowser) {
      document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
      localStorage.setItem(this.KEY, dark ? 'dark' : 'light');
    }
  }
}
