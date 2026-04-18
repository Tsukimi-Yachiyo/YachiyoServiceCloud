-- name: CreateScore :exec
insert into "YachiyoCup2026"(
id,
score
) values(
    $1,$2
)
;
-- name: GetScore :one
select score from "YachiyoCup2026"
where id  = $1 limit 1;

-- name: UpdataScore :exec
update "YachiyoCup2026"
set score = $2
where id = $1;

-- name: GetId :one
select id from "YachiyoCup2026"
where id  = $1 limit 1;

