import { TestBed } from '@angular/core/testing';

import { VehicleMapService } from './vehicle-map.service';

describe('VehicleMapService', () => {
  let service: VehicleMapService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VehicleMapService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
