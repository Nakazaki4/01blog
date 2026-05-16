import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Like } from './like';

describe('Like', () => {
  let component: Like;
  let fixture: ComponentFixture<Like>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Like],
    }).compileComponents();

    fixture = TestBed.createComponent(Like);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
