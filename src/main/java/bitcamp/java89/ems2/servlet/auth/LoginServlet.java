package bitcamp.java89.ems2.servlet.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bitcamp.java89.ems2.dao.ManagerDao;
import bitcamp.java89.ems2.dao.MemberDao;
import bitcamp.java89.ems2.dao.StudentDao;
import bitcamp.java89.ems2.dao.TeacherDao;
import bitcamp.java89.ems2.domain.Member;

@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException{
    
    String email = "";
    Cookie[] cookies = request.getCookies();
    
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("email")) {
          email = cookie.getValue();
          break;
        }
      }
    }
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("<head>");
    out.println("<meta charset='UTF-8'>");
    out.println("<title>로그인</title>");
    out.println("</head>");
    out.println("<body>");
    
    // HeaderServlet에게 머리말 HTML 생성을 요청한다.
    RequestDispatcher rd = request.getRequestDispatcher("/header");
    rd.include(request, response);
    
    out.println("<h1>로그인</h1>");
    out.println("<form action='login' method='POST'>");
    out.println("<table border='1'>");
    out.println("<tr>");
    out.println("  <th>회원 유형</th>");
    out.println("  <td>");
    out.println("    <input type='radio' name='userType' value='student' checked>학생");
    out.println("    <input type='radio' name='userType' value='teacher'>강사");
    out.println("    <input type='radio' name='userType' value='manager'>운영자");
    out.println("  </td>");
    out.println("</tr>");
    out.printf("<tr><th>이메일</th><td>"
        + "<input name='email' type='text' placeholder='예)hong@test.com'"
        + " value='%s'></td></tr>\n", email);
    out.println("<tr><th>암호</th><td><input name='password' type='password'></td></tr>");
    out.println("<tr><th></th><td><input name='saveEmail' type='checkbox'> 이메일 저장</td></tr>");
    out.println("</table>");
    
    out.println("<button type='submit'>로그인</button>");
    out.println("</form>");
    
    // FooterServlet에게 꼬리말 HTML 생성을 요청한다.
    rd = request.getRequestDispatcher("/footer");
    rd.include(request, response);
    
    out.println("</body>");
    out.println("</html>");
  }
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
    try {
      String email = request.getParameter("email");
      String password = request.getParameter("password");
      String saveEmail = request.getParameter("saveEmail");
      
      if (saveEmail != null) {
        // 쿠키를 웹 브라우저에게 보낸다.
        Cookie cookie = new Cookie("email", email);
        cookie.setMaxAge(60 * 60 * 24 * 15);
        response.addCookie(cookie);
        
      } else {
        // 기존에 보낸 쿠키를 제거하라고 웹 브라우저에게 응답한다.
        Cookie cookie = new Cookie("email", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
      }
      
      MemberDao memberDao = (MemberDao)this.getServletContext().getAttribute("memberDao");
      Member member = memberDao.getOne(email, password);
      
      
      
      if (member != null) {
        String userType = request.getParameter("userType");
        Member detailMember = this.getMemberInfo(userType, member.getMemberNo()); //HttpSession에 저장한다.
        
        if (detailMember != null) {
          request.getSession().setAttribute("member", detailMember); //HttpSession에 저장한다.
          return;
        }
      }
      
      response.setHeader("Refresh", "2;url=login");
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter out = response.getWriter();
      
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("<meta charset='UTF-8'>");
      out.println("<title>로그인</title>");
      out.println("</head>");
      out.println("<body>");
      
      // HeaderServlet에게 머리말 HTML 생성을 요청한다.
      RequestDispatcher rd = request.getRequestDispatcher("/header");
      rd.include(request, response);
      
      out.println("<h1>로그인 실패</h1>");
      out.println("<p>이메일 또는 암호가 일치하지 않거나, "
          + " 해당 유형의 사용자가 아닙니다.</p>");
      // FooterServlet에게 꼬리말 HTML 생성을 요청한다.
      rd = request.getRequestDispatcher("/footer");
      rd.include(request, response);
      
      out.println("</body>");
      out.println("</html>");
      
    } catch (Exception e) {
      request.setAttribute("error", e);
      RequestDispatcher rd = request.getRequestDispatcher("/error");
      rd.forward(request, response);
      return;
    }
  }
  
  private Member getMemberInfo(String userType, int memberNo) throws Exception {
    if (userType.equals(Member.STUDENT)) {
      StudentDao studentDao = (StudentDao)this.getServletContext().getAttribute("studentDao");
      return studentDao.getOne(memberNo);
      
    } else if (userType.equals(Member.TEACHER)) {
      TeacherDao teacherDao = (TeacherDao)this.getServletContext().getAttribute("teacherDao");
      return teacherDao.getOne(memberNo);
      
    } else /*if (userType.equals(Member.MANAGER))*/ {
      ManagerDao managerDao = (ManagerDao)this.getServletContext().getAttribute("managerDao");
      return managerDao.getOne(memberNo);
    }
  }
}




