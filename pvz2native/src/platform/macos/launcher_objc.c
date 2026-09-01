#import <Cocoa/Cocoa.h>
#include <string.h>
#include <pvz2native/platform/macos_launcher.h>

static NSString *trimmed(NSString *s) {
    return [s stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
}

static NSString *setting_key(NSString *line) {
    NSString *s = trimmed(line);

    /* Comments are documentation, never active settings. */
    if ([s hasPrefix:@";"] || [s hasPrefix:@"#"] || s.length == 0) {
        return nil;
    }

    NSRange eq = [s rangeOfString:@"="];
    if (eq.location == NSNotFound) return nil;
    return trimmed([s substringToIndex:eq.location]);
}

static void append_missing_video_keys(NSMutableArray<NSString *> *out,
                                      BOOL seenMode, BOOL seenWidth, BOOL seenHeight,
                                      BOOL seenFullscreen, BOOL seenFps,
                                      NSString *mode, NSInteger width, NSInteger height,
                                      BOOL fullscreen, NSInteger fps) {
    if (!seenMode)       [out addObject:[NSString stringWithFormat:@"mode = %@", mode]];
    if (!seenWidth)      [out addObject:[NSString stringWithFormat:@"width = %ld", (long)width]];
    if (!seenHeight)     [out addObject:[NSString stringWithFormat:@"height = %ld", (long)height]];
    if (!seenFullscreen) [out addObject:[NSString stringWithFormat:@"fullscreen = %d", fullscreen ? 1 : 0]];
    if (!seenFps)        [out addObject:[NSString stringWithFormat:@"fps_limit = %ld", (long)fps]];
}

static BOOL write_video_settings(const char *ini_path,
                                 NSString *mode, NSInteger width, NSInteger height,
                                 BOOL fullscreen, NSInteger fps) {
    if (!ini_path || !*ini_path) return NO;

    NSString *path = [NSString stringWithUTF8String:ini_path];
    NSError *error = nil;
    NSString *text = [NSString stringWithContentsOfFile:path
                                                encoding:NSUTF8StringEncoding
                                                   error:&error];
    if (!text) {
        NSLog(@"PvZ2Native launcher: cannot read config.ini: %@", error);
        return NO;
    }

    NSArray<NSString *> *lines = [text componentsSeparatedByCharactersInSet:
        [NSCharacterSet newlineCharacterSet]];
    NSMutableArray<NSString *> *out = [NSMutableArray arrayWithCapacity:lines.count + 8];

    BOOL inVideo = NO;
    BOOL foundVideo = NO;
    BOOL seenMode = NO, seenWidth = NO, seenHeight = NO, seenFullscreen = NO, seenFps = NO;

    for (NSString *line in lines) {
        NSString *t = trimmed(line);

        if ([t hasPrefix:@"["] && [t hasSuffix:@"]"]) {
            if (inVideo) {
                append_missing_video_keys(out, seenMode, seenWidth, seenHeight,
                                          seenFullscreen, seenFps,
                                          mode, width, height, fullscreen, fps);
            }

            inVideo = [t caseInsensitiveCompare:@"[video]"] == NSOrderedSame;
            if (inVideo) {
                foundVideo = YES;
                seenMode = seenWidth = seenHeight = seenFullscreen = seenFps = NO;
            }

            [out addObject:line];
            continue;
        }

        if (inVideo) {
            NSString *key = setting_key(line);

            if ([key isEqualToString:@"mode"]) {
                if (!seenMode) {
                    [out addObject:[NSString stringWithFormat:@"mode = %@", mode]];
                    seenMode = YES;
                }
                continue;
            }
            if ([key isEqualToString:@"width"]) {
                if (!seenWidth) {
                    [out addObject:[NSString stringWithFormat:@"width = %ld", (long)width]];
                    seenWidth = YES;
                }
                continue;
            }
            if ([key isEqualToString:@"height"]) {
                if (!seenHeight) {
                    [out addObject:[NSString stringWithFormat:@"height = %ld", (long)height]];
                    seenHeight = YES;
                }
                continue;
            }
            if ([key isEqualToString:@"fullscreen"]) {
                if (!seenFullscreen) {
                    [out addObject:[NSString stringWithFormat:@"fullscreen = %d", fullscreen ? 1 : 0]];
                    seenFullscreen = YES;
                }
                continue;
            }
            if ([key isEqualToString:@"fps_limit"] || [key isEqualToString:@"fpslimit"]) {
                if (!seenFps) {
                    [out addObject:[NSString stringWithFormat:@"fps_limit = %ld", (long)fps]];
                    seenFps = YES;
                }
                continue;
            }
        }

        [out addObject:line];
    }

    if (inVideo) {
        append_missing_video_keys(out, seenMode, seenWidth, seenHeight,
                                  seenFullscreen, seenFps,
                                  mode, width, height, fullscreen, fps);
    }

    if (!foundVideo) {
        [out addObject:@""];
        [out addObject:@"[video]"];
        [out addObject:[NSString stringWithFormat:@"mode = %@", mode]];
        [out addObject:[NSString stringWithFormat:@"width = %ld", (long)width]];
        [out addObject:[NSString stringWithFormat:@"height = %ld", (long)height]];
        [out addObject:[NSString stringWithFormat:@"fullscreen = %d", fullscreen ? 1 : 0]];
        [out addObject:[NSString stringWithFormat:@"fps_limit = %ld", (long)fps]];
    }

    NSString *updated = [out componentsJoinedByString:@"\n"];
    BOOL ok = [updated writeToFile:path atomically:YES encoding:NSUTF8StringEncoding error:&error];
    if (!ok) NSLog(@"PvZ2Native launcher: cannot write config.ini: %@", error);
    return ok;
}

int pvz2_macos_show_launcher(const char *ini_path, const pvz2_config_t *cfg) {
    @autoreleasepool {
        if (!cfg) return 1;

        [NSApplication sharedApplication];


        NSAlert *alert = [[NSAlert alloc] init];
        alert.messageText = @"PvZ2Native";
        alert.informativeText = @"Choose video settings before starting the game.";
        alert.alertStyle = NSAlertStyleInformational;

        NSView *view = [[NSView alloc] initWithFrame:NSMakeRect(0, 0, 360, 122)];

        NSTextField *resolutionLabel = [NSTextField labelWithString:@"Resolution"];
        resolutionLabel.frame = NSMakeRect(0, 91, 100, 22);
        [view addSubview:resolutionLabel];

        NSPopUpButton *resolution = [[NSPopUpButton alloc] initWithFrame:NSMakeRect(110, 88, 245, 28)];
        [resolution addItemsWithTitles:@[@"Auto", @"1280 × 720", @"1920 × 1080", @"Native"]];
        [view addSubview:resolution];

        if (cfg->video_width == 1280 && cfg->video_height == 720) {
            [resolution selectItemAtIndex:1];
        } else if (cfg->video_width == 1920 && cfg->video_height == 1080) {
            [resolution selectItemAtIndex:2];
        } else if (strcmp(cfg->video_mode, "native") == 0) {
            [resolution selectItemAtIndex:3];
        } else {
            [resolution selectItemAtIndex:0];
        }

        NSTextField *fpsLabel = [NSTextField labelWithString:@"FPS limit"];
        fpsLabel.frame = NSMakeRect(0, 53, 100, 22);
        [view addSubview:fpsLabel];

        NSPopUpButton *fpsPopup = [[NSPopUpButton alloc] initWithFrame:NSMakeRect(110, 50, 245, 28)];
        [fpsPopup addItemsWithTitles:@[@"60 FPS", @"120 FPS", @"Unlimited"]];
        if (cfg->fps_limit == 120) {
            [fpsPopup selectItemAtIndex:1];
        } else if (cfg->fps_limit == 0) {
            [fpsPopup selectItemAtIndex:2];
        } else {
            [fpsPopup selectItemAtIndex:0];
        }
        [view addSubview:fpsPopup];

        NSButton *fullscreen = [[NSButton alloc] initWithFrame:NSMakeRect(110, 12, 245, 24)];
        fullscreen.buttonType = NSButtonTypeSwitch;
        fullscreen.title = @"Fullscreen";
        fullscreen.state = cfg->video_fullscreen ? NSControlStateValueOn : NSControlStateValueOff;
        [view addSubview:fullscreen];

        alert.accessoryView = view;
        [alert addButtonWithTitle:@"Play"];
        [alert addButtonWithTitle:@"Cancel"];

        NSModalResponse response = [alert runModal];
        if (response != NSAlertFirstButtonReturn) return 0;

        NSString *mode = @"auto";
        NSInteger width = 0;
        NSInteger height = 0;

        switch (resolution.indexOfSelectedItem) {
            case 1:
                width = 1280;
                height = 720;
                break;
            case 2:
                width = 1920;
                height = 1080;
                break;
            case 3:
                mode = @"native";
                break;
            default:
                break;
        }

        NSInteger fps = 60;
        if (fpsPopup.indexOfSelectedItem == 1) fps = 120;
        else if (fpsPopup.indexOfSelectedItem == 2) fps = 0;

        if (!write_video_settings(ini_path, mode, width, height,
                                  fullscreen.state == NSControlStateValueOn, fps)) {
            NSAlert *errorAlert = [[NSAlert alloc] init];
            errorAlert.messageText = @"Could not save video settings";
            errorAlert.informativeText = @"PvZ2Native could not update config.ini.";
            [errorAlert runModal];
            return 0;
        }

        return 1;
    }
}
