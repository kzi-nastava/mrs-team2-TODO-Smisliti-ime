import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AdminProfile } from './admin-profile';
import { AdminService } from '../service/admin.service';

describe('AdminProfile', () => {
  let component: AdminProfile;
  let fixture: ComponentFixture<AdminProfile>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminProfile, HttpClientTestingModule],
      providers: [AdminService]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminProfile);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});