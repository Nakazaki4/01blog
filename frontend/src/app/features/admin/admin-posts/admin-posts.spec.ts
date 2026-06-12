import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { AdminPostsComponent } from './admin-posts';

describe('AdminPosts', () => {
  let component: AdminPostsComponent;
  let fixture: ComponentFixture<AdminPostsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminPostsComponent],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminPostsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
