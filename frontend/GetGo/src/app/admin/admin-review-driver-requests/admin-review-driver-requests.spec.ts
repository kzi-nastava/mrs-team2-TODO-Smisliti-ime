import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminReviewDriverRequests } from './admin-review-driver-requests';

describe('AdminReviewDriverRequests', () => {
  let component: AdminReviewDriverRequests;
  let fixture: ComponentFixture<AdminReviewDriverRequests>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminReviewDriverRequests]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminReviewDriverRequests);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
