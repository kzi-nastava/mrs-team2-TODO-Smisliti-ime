import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrderRide } from './order-ride';

describe('OrderRide', () => {
  let component: OrderRide;
  let fixture: ComponentFixture<OrderRide>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderRide]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrderRide);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
