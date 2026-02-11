import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRidePriceComponent } from './admin-ride-price.component';

describe('AdminRidePriceComponent', () => {
  let component: AdminRidePriceComponent;
  let fixture: ComponentFixture<AdminRidePriceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRidePriceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminRidePriceComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
