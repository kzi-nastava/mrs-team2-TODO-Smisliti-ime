import { TestBed } from '@angular/core/testing';

import { ActiveRideService } from './active-ride.service';

describe('ActiveRideService', () => {
  let service: ActiveRideService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ActiveRideService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
