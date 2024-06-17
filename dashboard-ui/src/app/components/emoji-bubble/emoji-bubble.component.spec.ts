import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmojiBubbleComponent } from './emoji-bubble.component';

describe('EmojiBubbleComponent', () => {
  let component: EmojiBubbleComponent;
  let fixture: ComponentFixture<EmojiBubbleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmojiBubbleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EmojiBubbleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
