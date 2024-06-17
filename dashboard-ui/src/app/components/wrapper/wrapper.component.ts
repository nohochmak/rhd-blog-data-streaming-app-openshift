import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  faPlay,
  faPause,
  faTimes,
  faExclamationCircle
} from '@fortawesome/free-solid-svg-icons';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { EmojiTrackerService } from '../../services/emoji-tracker.service';

@Component({
  selector: 'app-wrapper',
  templateUrl: './wrapper.component.html',
  styleUrls: ['./wrapper.component.scss']
})
export class WrapperComponent {
  topNSize = environment.topNSize;
  faPlay = faPlay;
  faPause = faPause;
  faTimes = faTimes;
  faExclamationCircle = faExclamationCircle;
  track = true;
  hasConnectionError$: Subject<boolean>;
  emojiCode$: Observable<string>;

  constructor(
    private route: ActivatedRoute,
    private emojiTrackerService: EmojiTrackerService
  ) {
    this.hasConnectionError$ = emojiTrackerService.hasConnectionError$;
    this.emojiCode$ = this.route.queryParams.pipe(
      map(params => params['emoji'])
    );
  }
}
