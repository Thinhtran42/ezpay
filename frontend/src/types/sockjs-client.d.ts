declare module 'sockjs-client' {
  interface SockJSOptions {
    server?: string;
    sessionId?: string | (() => string);
    transports?: string[];
  }

  interface SockJSClass {
    new (url: string, protocols?: string | string[], options?: SockJSOptions): SockJS;
  }

  interface SockJS extends EventTarget {
    send(data: string): void;
    close(code?: number, reason?: string): void;
    readyState: number;
    protocol: string;
    url: string;
    onopen: ((event: Event) => void) | null;
    onclose: ((event: CloseEvent) => void) | null;
    onmessage: ((event: MessageEvent) => void) | null;
    onerror: ((event: Event) => void) | null;
  }

  const SockJS: SockJSClass;
  export default SockJS;
} 