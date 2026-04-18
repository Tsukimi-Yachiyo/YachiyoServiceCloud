package api

import (
	"net/http"
	"github.com/gin-gonic/gin"

)
//获取积分接口函数
type GetscoreRequest struct {
	Id int64 `json:"id" binding:"required"` 
}



func (server *Server) GetScore (ctx *gin.Context) {

	var req GetscoreRequest
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(http.StatusBadRequest, errorResponse(err))
		return
	}

	score,err := server.store.GetScore(ctx,int32(req.Id)) 
	if err != nil {
		ctx.JSON(http.StatusBadRequest, errorResponse(err))
		return
	}
	ctx.JSON(http.StatusOK, score)
}
