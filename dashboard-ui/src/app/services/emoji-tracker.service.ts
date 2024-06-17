import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, EMPTY, timer, BehaviorSubject, } from 'rxjs';
import { share, tap, retryWhen, delayWhen } from 'rxjs/operators';

import { fromEventSource } from '../utils/rxjs.util';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmojiTrackerService {
  hasConnectionError$: Subject<boolean>;
  emojiUpdatesNotify$: Observable<EmojiData>;
  emojiTopN$: Observable<EmojiData[]>;
  apiHost = environment.apiHost;
  apiPort = environment.apiPort;

  constructor(private http: HttpClient) {
    
    this.hasConnectionError$ = new BehaviorSubject(false);
    
    this.emojiUpdatesNotify$ = fromEventSource<EmojiData>(
          `http://${this.apiHost}:${this.apiPort}/api/emojis/updates/notify`
        ).pipe(
          retryWhen(errors => {
            return errors.pipe(
              tap(() => {console.log("upps error :(");this.hasConnectionError$.next(true);}),
              delayWhen(() =>  {
                  console.log('backing off for a while...');      
                  return timer(2500).pipe(tap(() => {
                    this.hasConnectionError$.next(false);
                    console.log('trying to reconnect after error or timeout due to inactivity on the SSE stream');
                  }));
                }
              )
            );
            }
          ),
          share()
        );
    
    this.emojiTopN$ = this.http.get<EmojiData[]>(
        `http://${this.apiHost}:${this.apiPort}/api/emojis/stats/topN`
      )
      .pipe(
        retryWhen(errors => {
          return errors.pipe(
            delayWhen(() => timer(3500)),
            tap(() => {
              console.log('trying to reconnect after error');
            }));
          }
        ),
        share()
      );
  }

  
  emojiUpdatesNotify(): Observable<EmojiData> {
    return this.emojiUpdatesNotify$;
  }

  emojiTopN(): Observable<EmojiData[]> {
    return this.emojiTopN$;
  }

  emojiTootStream(emojiCode: string): Observable<TootData> {
    return fromEventSource<TootData>(
      `http://${this.apiHost}:${this.apiPort}/api/emojis/${emojiCode}/toots`
    ).pipe(
      retryWhen(errors => {
        return errors.pipe(
          tap(() => {console.log("upps error :(");this.hasConnectionError$.next(true);}),
          delayWhen(() =>  {
              console.log('backing off for a while...');      
              return timer(5000).pipe(tap(() => {
                this.hasConnectionError$.next(false);
                console.log('trying to reconnect after error or timeout due to inactivity on the SSE stream');
              }));
            }
          )
        );
        }
      ),
      share()
    );
  }

  handleError = (error, observable) => {
    this.hasConnectionError$.next(true);
    return EMPTY;
  }

  errorStream() {
    return this.hasConnectionError$;
  }
}
