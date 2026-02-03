import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SupportChatComponent } from './support-chat.component';

describe('SupportChatComponent', () => {
  let component: SupportChatComponent;
  let fixture: ComponentFixture<SupportChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SupportChatComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SupportChatComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
