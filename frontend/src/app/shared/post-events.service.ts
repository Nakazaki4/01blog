import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { PostResponse } from '../components/post-snippet/post-snippet';

@Injectable({ providedIn: 'root' })
export class PostEventsService {
  private created = new Subject<PostResponse>();
  created$ = this.created.asObservable();

  emitCreated(post: PostResponse): void {
    this.created.next(post);
  }
}
