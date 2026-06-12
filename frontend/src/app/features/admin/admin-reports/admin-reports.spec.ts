import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { AdminReportsComponent } from './admin-reports';

describe('AdminReports', () => {
  let component: AdminReportsComponent;
  let fixture: ComponentFixture<AdminReportsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminReportsComponent],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminReportsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
