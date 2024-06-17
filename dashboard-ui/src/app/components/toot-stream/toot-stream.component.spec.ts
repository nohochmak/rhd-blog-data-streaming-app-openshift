import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TootStreamComponent } from './toot-stream.component';

describe('TweetStreamComponent', () => {
  let component: TootStreamComponent;
  let fixture: ComponentFixture<TootStreamComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TootStreamComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TootStreamComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
