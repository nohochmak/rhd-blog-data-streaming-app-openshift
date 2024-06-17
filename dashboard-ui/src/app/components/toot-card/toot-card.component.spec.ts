import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TootCardComponent } from './toot-card.component';

describe('TootCardComponent', () => {
  let component: TootCardComponent;
  let fixture: ComponentFixture<TootCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TootCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TootCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
