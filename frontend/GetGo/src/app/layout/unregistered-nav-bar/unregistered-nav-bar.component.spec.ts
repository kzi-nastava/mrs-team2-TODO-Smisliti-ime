import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnregisteredNavBarComponent } from './unregistered-nav-bar.component';

describe('UnregisteredNavBarComponent', () => {
  let component: UnregisteredNavBarComponent;
  let fixture: ComponentFixture<UnregisteredNavBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnregisteredNavBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UnregisteredNavBarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
