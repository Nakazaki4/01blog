import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { AdminUsersComponent } from './admin-users';

describe('AdminUsers', () => {
  let component: AdminUsersComponent;
  let fixture: ComponentFixture<AdminUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminUsersComponent],
      providers: [provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminUsersComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
