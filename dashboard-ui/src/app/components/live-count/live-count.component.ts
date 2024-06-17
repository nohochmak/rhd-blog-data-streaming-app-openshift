import { Component, Input } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { queueUp } from '../../utils/rxjs.util';
import { EmojiTrackerService } from '../../services/emoji-tracker.service';

@Component({
  selector: 'app-live-count',
  templateUrl: './live-count.component.html',
  styleUrls: ['./live-count.component.scss'],
  animations: [
    trigger('incoming', [
      transition(':enter', [
        style({ maxHeight: '0' }),
        animate(600, style({ maxHeight: '30rem' }))
      ])
    ])
  ]
})
export class LiveCountComponent {
  @Input() track = true;
  queueSize = environment.queueSize;
  emojiUpdateQueue$: Observable<EmojiData[]>;

  constructor(private emojiTrackerService: EmojiTrackerService) {
    this.emojiUpdateQueue$ = emojiTrackerService.emojiUpdatesNotify().pipe(
      filter(() => this.track),
      queueUp(this.queueSize)
    );
  }

  trackBy(index: number, emojiData: EmojiData) {
    return `${emojiData.emoji}_${emojiData.count}`;
  }
}
