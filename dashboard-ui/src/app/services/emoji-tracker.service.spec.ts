import { TestBed } from '@angular/core/testing';

import { EmojiTrackerService } from './emoji-tracker.service';

describe('EmojiTrackerService', () => {
  let service: EmojiTrackerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EmojiTrackerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
