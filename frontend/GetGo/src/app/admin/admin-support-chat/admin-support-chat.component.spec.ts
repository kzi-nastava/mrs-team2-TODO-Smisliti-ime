import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminSupportChatComponent } from './admin-support-chat.component';

describe('AdminSupportChatComponent', () => {
  let component: AdminSupportChatComponent;
  let fixture: ComponentFixture<AdminSupportChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminSupportChatComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminSupportChatComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
