# Instagram export compatibility

This document describes what InstaChecker accepts and how relationship files are selected.

## What Meta documents

Meta's [official Instagram Help Center article](https://www.facebook.com/help/instagram/181231772500920)
documents the Accounts Center export workflow. Instagram exports are offered in HTML or JSON. The
official help material does not define a stable internal ZIP directory tree or promise fixed
relationship filenames.

For that reason, directory names are treated as opaque and file selection is based on basenames,
configured overrides, and recognizable content markers.

## Supported formats

| Format | Status | Recognized account data |
|---|---|---|
| JSON | Official export format | `string_list_data` entries containing `value` and `href` |
| HTML / HTM | Official export format | Instagram profile links in anchor elements |
| XML | Compatibility only | Profile anchors or paired `<value>` and `<href>` elements |

XML is not a documented Instagram export option. Its parser exists for converted archives and for
future compatibility, but no fixed Instagram XML schema is assumed.

## Selection algorithm

1. Open the selected ZIP as a stream; do not write entries to disk.
2. Walk every entry regardless of directory depth.
3. Ignore directories and unsupported extensions.
4. Prefer exact configured basenames (`followers_1` and `following` by default).
5. Include numbered siblings of a configured follower file, such as `followers_2`.
6. Fall back to known `followers[_N]` and `following[_N]` basenames.
7. As a final fallback, use unambiguous JSON keys or HTML/XML document labels.
8. Parse the best-priority matches and deduplicate usernames case-insensitively.

Unrelated files—including profile data, comments, media, and other connection lists—do not take
part in the comparison.

## JSON shapes

A follower file is normally an array:

```json
[
  {
    "string_list_data": [
      {
        "href": "https://www.instagram.com/example/",
        "value": "example",
        "timestamp": 1700000000
      }
    ]
  }
]
```

A following file normally wraps the same entries:

```json
{
  "relationships_following": [
    {
      "string_list_data": [
        {
          "href": "https://www.instagram.com/example/",
          "value": "example",
          "timestamp": 1700000000
        }
      ]
    }
  ]
}
```

Unknown properties are ignored so additional metadata does not break an import.

## Import settings

The two name overrides are saved locally with Android `SharedPreferences` and remain active across
app launches. An extension is optional. If the configured follower name ends in a number, such as
`followers_1`, every sibling matching the same numbered series is imported.

Use **Restore defaults** to return to `followers_1` and `following`.

## Failure behavior

The importer returns a user-readable error when the ZIP is empty, invalid, oversized, missing one
of the required relationship roles, or contains a selected relationship file that cannot be parsed.
No analysis page is stored until discovery and parsing have completed successfully.
