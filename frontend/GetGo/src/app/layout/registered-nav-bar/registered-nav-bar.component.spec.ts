import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisteredNavBarComponent } from './registered-nav-bar.component';

describe('RegisteredNavBarComponent', () => {
  let component: RegisteredNavBarComponent;
  let fixture: ComponentFixture<RegisteredNavBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisteredNavBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisteredNavBarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
