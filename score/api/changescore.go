package api

import (
	"net/http"
	db "score/sqlc"

	"github.com/gin-gonic/gin"
)
//更新积分端口函数
type ChangeScoreRequest struct {
	ID    int32		`json:"id" binding:"required"`
	Score int32		`json:"score" binding:"required"`
}


func (server *Server) ChangeScore (ctx *gin.Context) {

	var req ChangeScoreRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(http.StatusBadRequest, errorResponse(err))
		return
	}
	arg := db.UpdataScoreParams{
		ID: req.ID,
		Score: req.Score,
	}

	_,err := server.store.GetId(ctx,arg.ID) 
	if err != nil {
		ctx.JSON(http.StatusBadRequest, errorResponse(err))
		return
	}
	err = server.store.UpdataScore(ctx,arg)
	if err != nil {
		ctx.JSON(http.StatusBadRequest, errorResponse(err))
		return
	}


	ctx.JSON(http.StatusOK, "success")
}
