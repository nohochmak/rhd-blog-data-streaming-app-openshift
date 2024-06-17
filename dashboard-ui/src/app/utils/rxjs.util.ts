import { Observable } from 'rxjs';

export const fromEventSource = <T>(url: string): Observable<T> => {
  console.log("fromEventSource called for URL "+url);
  return new Observable(observer => {
    const eventSource = new EventSource(url);
    eventSource.addEventListener('message', (event: MessageEvent) =>
      observer.next(JSON.parse(event.data) as T)
    );
    eventSource.addEventListener('error', error => observer.error(error));
    return {
      unsubscribe: () => {
        eventSource.close();
      }
    };
  });
};

export const queueUp = (maxLength: number, reset$: Observable<any> = null) => {
  return <T>(source: Observable<T>): Observable<T[]> =>
    new Observable(observer => {
      let queue: T[] = [];
      if (reset$) {
        reset$.subscribe(() => {
          queue = [];
          observer.next(queue);
        });
      }
      return source.subscribe(
        (value: T) => {
          try {
            queue.unshift(value);
            if (queue.length > maxLength) {
              var last = queue.pop();
            }
            observer.next(queue);
          } catch (err) {
            observer.error(err);
          }
        },
        err => observer.error(err),
        () => observer.complete()
      );
    });
};
