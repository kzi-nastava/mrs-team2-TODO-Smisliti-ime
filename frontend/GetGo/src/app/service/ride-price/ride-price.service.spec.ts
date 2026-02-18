import { TestBed } from '@angular/core/testing';

import { RidePriceService } from './ride-price.service';

describe('RidePriceService', () => {
  let service: RidePriceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RidePriceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
