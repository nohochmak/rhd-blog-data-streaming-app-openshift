import { Component, Input } from '@angular/core';
import { Observable } from 'rxjs';
import { concatMapTo, distinctUntilChanged, filter } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { EmojiTrackerService } from '../../services/emoji-tracker.service';

@Component({
  selector: 'app-top-n',
  templateUrl: './top-n.component.html',
  styleUrls: ['./top-n.component.scss']
})
export class TopNComponent {
  @Input() track = true;
  topNSize = environment.topNSize;
  emojiUpdate$: Observable<EmojiData>;
  emojiTopN$: Observable<EmojiData[]>;
  currentTopN$: Observable<EmojiData[]>;

  constructor(private emojiTrackerService: EmojiTrackerService) {
    this.emojiTopN$ = this.emojiTrackerService.emojiTopN();
    this.emojiUpdate$ = this.emojiTrackerService
      .emojiUpdatesNotify()
      .pipe(filter(() => this.track));
    this.currentTopN$ = this.emojiTopN$.pipe(
      concatMapTo(this.emojiUpdate$, (emojiMap, updateEmojiData) => {
        const emojiIndex = emojiMap.findIndex(
          val => val.emoji === updateEmojiData.emoji
        );
        if (emojiIndex > -1) {
          emojiMap[emojiIndex].count = updateEmojiData.count;
        } else {
          emojiMap.push(updateEmojiData);
        }
        return emojiMap;
      }),
      distinctUntilChanged()
    );
  }

  trackBy(index: number, emojiData: EmojiData) {
    return `${index}_${emojiData.emoji}_${emojiData.count}`;
  }
}
