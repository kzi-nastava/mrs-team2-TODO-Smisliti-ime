import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FavoriteRides } from './favorite-rides';

describe('FavoriteRides', () => {
  let component: FavoriteRides;
  let fixture: ComponentFixture<FavoriteRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FavoriteRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FavoriteRides);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
