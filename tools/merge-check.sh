#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/.." && pwd)"
cd "$repo_root"

marker='INTERNAL-TERMUX MODIFIED - merge carefully'

resolve_default_base_ref() {
    local candidate
    for candidate in upstream/main remotes/upstream/main origin/main remotes/origin/main; do
        if git rev-parse --verify --quiet "${candidate}^{commit}" >/dev/null; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    return 1
}

contains_file() {
    local needle="$1"
    shift || true
    local item
    for item in "$@"; do
        if [[ "$item" == "$needle" ]]; then
            return 0
        fi
    done
    return 1
}

classify_tagged_file() {
    local file="$1"
    local ours
    local ancestor
    local theirs

    if git diff --quiet "$merge_base" "$head_ref" -- "$file"; then
        printf 'REVIEW\n'
        return 0
    fi

    if ! git cat-file -e "${merge_base}:${file}" 2>/dev/null || \
        ! git cat-file -e "${head_ref}:${file}" 2>/dev/null || \
        ! git cat-file -e "${base_ref}:${file}" 2>/dev/null; then
        printf 'REVIEW\n'
        return 0
    fi

    ours="$(mktemp)"
    ancestor="$(mktemp)"
    theirs="$(mktemp)"
    trap 'rm -f "$ours" "$ancestor" "$theirs"' RETURN

    git show "${head_ref}:${file}" >"$ours"
    git show "${merge_base}:${file}" >"$ancestor"
    git show "${base_ref}:${file}" >"$theirs"

    if git merge-file -p "$ours" "$ancestor" "$theirs" >/dev/null 2>&1; then
        printf 'REVIEW\n'
    else
        printf 'CONFLICT\n'
    fi
}

base_ref="${1:-}"
if [[ -z "$base_ref" ]]; then
    if ! base_ref="$(resolve_default_base_ref)"; then
        echo "Unable to resolve a default base ref." >&2
        echo "Pass refs explicitly, for example: tools/merge-check.sh upstream/main HEAD" >&2
        exit 1
    fi
fi

head_ref="${2:-HEAD}"

if ! git rev-parse --verify --quiet "${base_ref}^{commit}" >/dev/null; then
    echo "Unknown base ref: $base_ref" >&2
    exit 1
fi

if ! git rev-parse --verify --quiet "${head_ref}^{commit}" >/dev/null; then
    echo "Unknown head ref: $head_ref" >&2
    exit 1
fi

merge_base="$(git merge-base "$base_ref" "$head_ref")"

mapfile -t tagged_candidates < <(git grep -l "$marker" -- . 2>/dev/null | grep -v '^upstream/' | grep -v '/build/' || true)
tagged_files=()
for file in "${tagged_candidates[@]}"; do
    [[ -z "$file" ]] && continue
    first_match="$(git grep -n -m1 "$marker" -- "$file" || true)"
    line_number="$(printf '%s\n' "$first_match" | head -n1 | cut -d: -f2)"
    if [[ -n "$line_number" && "$line_number" -le 5 ]]; then
        tagged_files+=("$file")
    fi
done

mapfile -t upstream_changed_files < <(git diff --name-only "$merge_base" "$base_ref" -- . | grep -v '^upstream/' | grep -v '/build/' || true)

safe_files=()
review_files=()
conflict_files=()

for file in "${upstream_changed_files[@]}"; do
    [[ -z "$file" ]] && continue
    if contains_file "$file" "${tagged_files[@]}"; then
        case "$(classify_tagged_file "$file")" in
            REVIEW)
                review_files+=("$file")
                ;;
            CONFLICT)
                conflict_files+=("$file")
                ;;
        esac
    else
        safe_files+=("$file")
    fi
done

echo "merge-check base: $base_ref"
echo "merge-check head: $head_ref"
echo "merge-base: $merge_base"

if [[ "${#upstream_changed_files[@]}" -eq 0 ]]; then
    echo "SAFE    no upstream-tracked file changes detected"
    exit 0
fi

for file in "${safe_files[@]}"; do
    printf 'SAFE    %s\n' "$file"
done

for file in "${review_files[@]}"; do
    printf 'REVIEW  %s\n' "$file"
done

for file in "${conflict_files[@]}"; do
    printf 'CONFLICT %s\n' "$file"
done

if [[ "${#review_files[@]}" -eq 0 && "${#conflict_files[@]}" -eq 0 ]]; then
    exit 0
fi

exit 2
