import { TestBed } from '@angular/core/testing';

import { RideTrackingService } from './ride-tracking.service';

describe('RideTrackingService', () => {
  let service: RideTrackingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RideTrackingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
