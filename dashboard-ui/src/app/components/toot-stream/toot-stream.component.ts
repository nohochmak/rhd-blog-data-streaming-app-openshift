import { Component, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { trigger, transition, style, animate } from '@angular/animations';
import { Observable } from 'rxjs';
import {
  map,
  share,
  switchMap,
  distinctUntilChanged,
  filter,
  tap,
  shareReplay
} from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { queueUp } from '../../utils/rxjs.util';
import { EmojiTrackerService } from '../../services/emoji-tracker.service';

@Component({
  selector: 'app-tweet-stream',
  templateUrl: './toot-stream.component.html',
  styleUrls: ['./toot-stream.component.scss']
})
export class TootStreamComponent {
  @Input() track = true;
  queueSize = environment.queueSize;
  emojiCode$: Observable<string>;
  emojiTootStream$: Observable<TootData[]>;

  constructor(
    private route: ActivatedRoute,
    private emojiTrackerService: EmojiTrackerService
  ) {
    this.emojiCode$ = this.route.queryParams.pipe(
      map(params => params['emoji']),
      distinctUntilChanged(),
      shareReplay()
    );
    this.emojiTootStream$ = this.emojiCode$.pipe(
      switchMap(emojiCode => {
        return this.emojiTrackerService.emojiTootStream(emojiCode);
      }),
      filter(() => this.track),
      queueUp(this.queueSize, this.emojiCode$),
      share()
    );
  }
}
