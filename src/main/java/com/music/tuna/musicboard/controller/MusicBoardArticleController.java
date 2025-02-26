package com.music.tuna.musicboard.controller;

import com.music.tuna.member.model.service.IdMissMatchException;
import com.music.tuna.member.model.vo.Member;
import com.music.tuna.musicboard.service.MusicBoardArticleService;
import com.music.tuna.musicboard.vo.MusicBoardArticleListPage;
import com.music.tuna.musicboard.vo.MusicBoardArticle;
import com.music.tuna.util.SHBoardFileUpload;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
@Controller
public class MusicBoardArticleController {
    @Autowired
    MusicBoardArticleService musicBoardArticleService;
    @RequestMapping(value = "/musicBoard/article/write.do", method = RequestMethod.GET)
    public ModelAndView insertArticleGet(ModelAndView mv){

        mv.setViewName("/musicBoard/write");
        mv.addObject("uri", "write.do");
        return mv;
    }
    @RequestMapping(value = "/musicBoard/article/write.do", method = RequestMethod.POST)
    public String insertArticlePost(MusicBoardArticle vo, HttpServletRequest request){
        int articleNo = 0;
            try {
                vo.setFileName(SHBoardFileUpload.fileUpload(vo.getUploadFile(), request.getSession().getServletContext().getRealPath("/resources/upload/")));
                vo.setAlbumFile(SHBoardFileUpload.fileUpload(vo.getAlbumUploadFile(), request.getSession().getServletContext().getRealPath("/resources/albumImageUpload/")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        articleNo = musicBoardArticleService.insertArticle(vo);
        return "redirect:read.do?articleNo="+articleNo;
    }
    @RequestMapping(value = "/musicBoard/article/read.do")
    public ModelAndView getArticle(ModelAndView mv, MusicBoardArticle vo){
        mv.setViewName("/musicBoard/read");
        mv.addObject("article", musicBoardArticleService.getArticle(vo));
        return mv;
    }
    @RequestMapping(value="/musicBoard/article/list.do")
    public ModelAndView getList(ModelAndView mv, MusicBoardArticleListPage vo){
        int totalCount = musicBoardArticleService.getCount();
        //한 화면에 표시될 게시글의 최대 개수
        int listCount = 16;
        //전체 게시글 개수를 최대 개수로 나누어 전체 페이지 수를 구함
        int totalPage = totalCount/listCount;
        if(vo.getPage() == 0){
            vo.setPage(1);
        }

        //전체 페이지수가 딱 떨어지지 않을 경우 나머지를 표시해줘야하기때문에 +1을 해줌
        if(totalCount % listCount > 0){
            totalPage++;
        }
        if(totalPage < vo.getPage()){
            vo.setPage(totalPage);
        }

        int pageCount = 5;
        vo.setStartPage(((vo.getPage() -1)/ pageCount) * pageCount +1);
        vo.setEndPage(vo.getStartPage() + pageCount -1);
        if(vo.getEndPage() > totalPage){
           vo.setEndPage(totalPage);
        }
        vo.setEnd(vo.getPage() * listCount);
        vo.setStart(vo.getEnd() - listCount+1);
        vo.setPageContent(musicBoardArticleService.getArticleList(vo));
        mv.setViewName("/musicBoard/list");
        mv.addObject("articlePage", vo);
        return mv;
    }
    @RequestMapping("/musicBoard/article/best.do")
    public void updateBest(MusicBoardArticle vo, HttpServletResponse res){
        JSONObject json = new JSONObject();
        json.put("best", musicBoardArticleService.updateBest(vo));
        res.setContentType("application/x-json; charset=utf-8");
        try{
            res.getWriter().print(json);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @RequestMapping("/musicBoard/article/bad.do")
    public void updateBad(MusicBoardArticle vo, HttpServletResponse res){
        JSONObject json = new JSONObject();
        res.setContentType("application/x-json; charset=utf-8");
        json.put("bad", musicBoardArticleService.updateBad(vo));
        try{
            res.getWriter().print(json);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @RequestMapping("/musicBoard/article/myList.do")
    public void selectMyWrittenList(HttpSession httpSession, HttpServletResponse res){
        JSONObject json = new JSONObject();
        res.setContentType("application/x-json; charset=utf-8");
        MusicBoardArticle musicBoardArticle = new MusicBoardArticle();
        musicBoardArticle.setId(((Member)httpSession.getAttribute("loginUser")).getUserId());
        json.put("result", musicBoardArticleService.getMyWrittenList(musicBoardArticle));
        try{
            res.getWriter().print(json);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @RequestMapping("/musicBoard/article/remove.do")
    public String deleteArticle(MusicBoardArticle vo, HttpSession httpSession){
        vo = musicBoardArticleService.getArticle(vo);
        String userId = ((Member)httpSession.getAttribute("loginUser")).getUserId();
        if(!vo.getId().equals(userId)) throw new IdMissMatchException();
        musicBoardArticleService.deleteArticle(vo);
        return "redirect:list.do";
    }
    @RequestMapping("/musicBoard/article/mydelete.do")//mypage 내에서 업로드 뮤직 삭제
    public void deleteMyArticle(MusicBoardArticle vo){
    	musicBoardArticleService.deleteArticle(vo);
    }
    @RequestMapping(value = "/musicBoard/article/edit.do", method = RequestMethod.GET)
    public ModelAndView editArticleGet(ModelAndView mv, MusicBoardArticle vo, HttpSession httpSession){
        vo = musicBoardArticleService.getArticle(vo);
        mv.setViewName("/musicBoard/write");
        mv.addObject("uri", "edit.do");
        mv.addObject("article", vo);
        return mv;
    }
    @RequestMapping(value = "/musicBoard/article/edit.do", method = RequestMethod.POST)
    public String editArticlePost(ModelAndView mv, MusicBoardArticle vo, HttpSession httpSession, HttpServletRequest request){
        String userId = ((Member)httpSession.getAttribute("loginUser")).getUserId();
        if(!vo.getId().equals(userId)) throw new IdMissMatchException();
        try {
            vo.setFileName(SHBoardFileUpload.fileUpload(vo.getUploadFile(), request.getSession().getServletContext().getRealPath("/resources/upload/")));
            vo.setAlbumFile(SHBoardFileUpload.fileUpload(vo.getAlbumUploadFile(), request.getSession().getServletContext().getRealPath("/resources/albumImageUpload/")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        musicBoardArticleService.updateArticle(vo);
        return "redirect:read.do?articleNo="+vo.getArticleNo();
    }
}
