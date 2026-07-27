/* libc.so -- <sys/socket.h> and friends.
 *
 * New in 4.5.2, which links Crashlytics and EA's analytics; 1.6 imports none of
 * these and plays perfectly without them. So this port has no network, and the
 * question is only how to SAY so.
 *
 * It says so by failing, immediately and permanently: socket() returns -1 with
 * EAFNOSUPPORT and every call that could only have been reached with a valid
 * descriptor returns -1 with EBADF. The alternative -- pretending a socket was
 * created and then never delivering data -- is the trap this project has been
 * caught by twice (see the FEATURE_UNSUPPORTED note in libopensles.cpp, and the
 * null-jstring one in the DEX layer): code that believes it succeeded goes on to
 * wait for something that will never arrive, and the symptom surfaces far away
 * from the cause. A reporting library that cannot open a socket, by contrast,
 * has a well-trodden error path -- it is what happens on a device in aeroplane
 * mode.
 *
 * Two exceptions, because they are honest work rather than a refusal:
 * inet_addr() is pure string parsing and needs no network at all, and
 * gethostname() has a real answer.
 *
 * If a future version turns out to need a working socket, this is the file to
 * replace -- the failure is confined here and announced in the log.
 */

#include <pvz2native/dependencies/dependency.h>

#include <cstdio>
#include <cstring>

namespace pvz2native {
namespace {

/* Linux/bionic values -- the guest compares against its own headers' numbers,
 * not the host's. */
constexpr std::uint32_t kEBADF = 9;
constexpr std::uint32_t kEAFNOSUPPORT = 97;

constexpr std::uint32_t kMinusOne = (std::uint32_t)-1;

/* Announced once rather than per call: a reporting library retries, and the
 * point is to make "this build has no network" visible in the log exactly once,
 * not to drown it. */
void note_no_network(GuestCall &c, const char *what) {
    static bool said = false;
    if (said) return;
    said = true;
    c.log("[net] %s -- this port has no network stack; sockets fail with EAFNOSUPPORT "
          "(see src/dependencies/libc_socket.cpp)", what);
}

void s_socket(GuestCall &c) {
    note_no_network(c, "socket()");
    c.set_errno(kEAFNOSUPPORT);
    c.set_result(kMinusOne);
}

/* Every one of these needs a descriptor socket() never handed out. */
void s_ebadf(GuestCall &c) {
    c.set_errno(kEBADF);
    c.set_result(kMinusOne);
}

/* select(nfds, readfds, writefds, exceptfds, timeout).
 *
 * 0 -- "the timeout expired and nothing is ready" -- rather than the -1 the
 * calls above return. A caller that gets -1 from select() has hit an error it
 * may well retry immediately in a tight loop; "nothing ready yet" is both the
 * truthful answer when no descriptor exists and the one that leaves normal
 * timeout handling in charge. */
void s_select(GuestCall &c) { c.set_result(0); }

/* in_addr_t inet_addr(const char *cp) -- dotted quad to a NETWORK-order 32-bit
 * address, or INADDR_NONE (0xFFFFFFFF) if it does not parse. No network
 * involved, so this is implemented rather than refused.
 *
 * Deliberately the strict four-part form only: inet_addr historically also
 * accepts "a", "a.b" and "a.b.c" with the trailing part widened, but nothing
 * writes addresses that way today and accepting them silently turns a typo into
 * a wrong address rather than an error. */
void s_inet_addr(GuestCall &c) {
    const std::string text = c.cstr(c.arg(0), 64);
    unsigned parts[4];
    char extra = 0;
    if (std::sscanf(text.c_str(), "%u.%u.%u.%u%c", &parts[0], &parts[1], &parts[2], &parts[3],
                    &extra) != 4) {
        c.set_result(kMinusOne); /* INADDR_NONE */
        return;
    }
    std::uint32_t addr = 0;
    for (int i = 0; i < 4; ++i) {
        if (parts[i] > 255) {
            c.set_result(kMinusOne);
            return;
        }
        /* Network byte order is big-endian, and the guest is little-endian, so
         * the first part ends up in the LOW byte of the returned word. */
        addr |= (std::uint32_t)parts[i] << (8 * i);
    }
    c.set_result(addr);
}

/* int gethostname(char *name, size_t len) */
void s_gethostname(GuestCall &c) {
    const std::uint32_t buf = c.arg(0);
    const std::uint32_t len = c.arg(1);
    static const char kName[] = "localhost";
    if (buf == 0 || len == 0) {
        c.set_result(kMinusOne);
        return;
    }
    const std::size_t n = sizeof(kName) - 1;
    if (len <= n) {
        /* Truncation is an error, and the buffer is left alone. */
        c.set_errno(28 /* ENAMETOOLONG */);
        c.set_result(kMinusOne);
        return;
    }
    c.put_cstr(buf, kName);
    c.set_result(0);
}

/* --- what 9.6.1 adds --------------------------------------------------------
 *
 * Two groups, treated differently on purpose: name RESOLUTION is refused,
 * because it needs a network; address FORMATTING is implemented, because it is
 * string manipulation that happens to be declared in a networking header. */

constexpr std::uint32_t kEAI_FAIL = 4; /* non-recoverable name resolution failure */

/* int getaddrinfo(node, service, hints, struct addrinfo **res)
 *
 * Non-zero is the error channel here -- getaddrinfo does NOT use errno -- and
 * *res is left NULL so a caller that ignores the return value still cannot walk
 * a bogus list. EAI_FAIL rather than EAI_AGAIN: "again" invites an immediate
 * retry loop, and no amount of retrying will produce a resolver. */
void s_getaddrinfo(GuestCall &c) {
    note_no_network(c, "getaddrinfo()");
    if (c.arg(3) != 0) c.write32(c.arg(3), 0);
    c.set_result(kEAI_FAIL);
}

void s_getnameinfo(GuestCall &c) {
    note_no_network(c, "getnameinfo()");
    c.set_result(kEAI_FAIL);
}

/* freeaddrinfo(res): getaddrinfo never allocated a list, so there is nothing to
 * release. Must still exist and must not fault -- it is called on the error
 * path of code that did not check. */
void s_freeaddrinfo(GuestCall &c) {}

void s_gai_strerror(GuestCall &c) {
    static std::uint32_t slot = 0;
    if (slot == 0) slot = c.rt->heap.alloc(64);
    c.put_cstr(slot, "Name resolution unavailable");
    c.set_result(slot);
}

/* struct hostent *gethostbyname(const char *) -- NULL means "not found", which
 * is the whole truth without a resolver. */
void s_gethostbyname(GuestCall &c) {
    note_no_network(c, "gethostbyname()");
    c.set_result(0);
}

/* unsigned if_nametoindex(const char *) -- 0 means "no interface by that name",
 * the specified way to report failure. */
void s_if_nametoindex(GuestCall &c) { c.set_result(0); }

/* socketpair(domain, type, protocol, int sv[2])
 *
 * Refused, unlike pipe() in libc_unistd.cpp which IS implemented. The
 * difference is real: a pipe is unidirectional and a host pipe reproduces it
 * exactly, while a socketpair is bidirectional and cannot be built from the
 * host primitives available here. Half a socketpair would drop everything sent
 * in one direction -- a data-loss bug that surfaces far from this file --
 * whereas a refusal lands on the caller's existing no-socket path. */
void s_socketpair(GuestCall &c) {
    note_no_network(c, "socketpair()");
    c.set_errno(kEAFNOSUPPORT);
    c.set_result(kMinusOne);
}

/* int inet_pton(int af, const char *src, void *dst) -- presentation to binary.
 * 1 on success, 0 for "not parseable in this family", -1 for a family we do not
 * know. No network involved, so this is implemented rather than refused. */
void s_inet_pton(GuestCall &c) {
    constexpr std::uint32_t kAF_INET = 2;
    const std::uint32_t af = c.arg(0), dst = c.arg(2);
    if (af != kAF_INET) {
        c.set_errno(kEAFNOSUPPORT);
        c.set_result(kMinusOne);
        return;
    }
    const std::string text = c.cstr(c.arg(1), 64);
    unsigned parts[4];
    char extra = 0;
    if (std::sscanf(text.c_str(), "%u.%u.%u.%u%c", &parts[0], &parts[1], &parts[2], &parts[3],
                    &extra) != 4) {
        c.set_result(0);
        return;
    }
    for (int i = 0; i < 4; ++i) {
        if (parts[i] > 255) {
            c.set_result(0);
            return;
        }
    }
    /* Network byte order: the first part is the FIRST byte in memory. */
    if (dst != 0) {
        for (int i = 0; i < 4; ++i) c.write8(dst + (std::uint32_t)i, (std::uint8_t)parts[i]);
    }
    c.set_result(1);
}

/* const char *inet_ntop(int af, const void *src, char *dst, socklen_t size)
 * Returns dst on success, NULL with ENOSPC if it does not fit. */
void s_inet_ntop(GuestCall &c) {
    constexpr std::uint32_t kAF_INET = 2;
    const std::uint32_t af = c.arg(0), src = c.arg(1), dst = c.arg(2), size = c.arg(3);
    if (af != kAF_INET) {
        c.set_errno(kEAFNOSUPPORT);
        c.set_result(0);
        return;
    }
    char text[16];
    std::snprintf(text, sizeof(text), "%u.%u.%u.%u", c.read8(src), c.read8(src + 1),
                  c.read8(src + 2), c.read8(src + 3));
    if (dst == 0 || size < std::strlen(text) + 1) {
        c.set_errno(28 /* ENOSPC */);
        c.set_result(0);
        return;
    }
    c.put_cstr(dst, text);
    c.set_result(dst);
}

}  // namespace

void register_libc_socket(ImportTable &t) {
    t.add("socket", s_socket);

    t.add("accept", s_ebadf);
    t.add("bind", s_ebadf);
    t.add("connect", s_ebadf);
    t.add("getsockname", s_ebadf);
    t.add("listen", s_ebadf);
    t.add("recv", s_ebadf);
    t.add("recvfrom", s_ebadf);
    t.add("send", s_ebadf);
    t.add("sendto", s_ebadf);
    t.add("setsockopt", s_ebadf);
    t.add("shutdown", s_ebadf);

    t.add("select", s_select);
    t.add("inet_addr", s_inet_addr);
    t.add("gethostname", s_gethostname);

    /* --- added for 9.6.1 --- */
    t.add("getpeername", s_ebadf);
    t.add("getsockopt", s_ebadf);
    t.add("socketpair", s_socketpair);

    t.add("getaddrinfo", s_getaddrinfo);
    t.add("getnameinfo", s_getnameinfo);
    t.add("freeaddrinfo", s_freeaddrinfo);
    t.add("gai_strerror", s_gai_strerror);
    t.add("gethostbyname", s_gethostbyname);
    t.add("if_nametoindex", s_if_nametoindex);

    /* Pure conversion, no network -- implemented, like inet_addr above. */
    t.add("inet_pton", s_inet_pton);
    t.add("inet_ntop", s_inet_ntop);
}

}  // namespace pvz2native
