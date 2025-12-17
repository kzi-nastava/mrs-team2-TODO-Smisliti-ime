import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserNavBarComponent } from './user-nav-bar.component';

describe('UserNavBarComponent', () => {
  let component: UserNavBarComponent;
  let fixture: ComponentFixture<UserNavBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserNavBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserNavBarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
