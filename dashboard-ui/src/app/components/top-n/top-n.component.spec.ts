import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TopNComponent } from './top-n.component';

describe('TopNComponent', () => {
  let component: TopNComponent;
  let fixture: ComponentFixture<TopNComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TopNComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TopNComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
