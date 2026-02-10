import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BlockUsers } from './block-users';

describe('BlockUsers', () => {
  let component: BlockUsers;
  let fixture: ComponentFixture<BlockUsers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BlockUsers]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BlockUsers);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
