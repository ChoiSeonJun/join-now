package com.exam.dao;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.exam.dto.AcceptDTO;
import com.exam.dto.PageDTO;
import com.exam.dto.PostDTO;

@Repository
public class AcceptDAO {
	@Autowired
	SqlSessionTemplate session;
	
	public int totalCount() {
		return session.selectOne("AcceptMapper.totalCount");
	}
	
	public PageDTO selectList(int curPage){
		PageDTO pageDTO = new PageDTO();
		pageDTO.setPerPage(10);
		int offset = (curPage-1)*pageDTO.getPerPage();
		int limit = pageDTO.getPerPage();
		List<PostDTO> list =  session.selectList("AcceptMapper.selectList", null, new RowBounds(offset, limit));
		
		pageDTO.setList(list);
		pageDTO.setCurPage(curPage);
		pageDTO.setTotalCount(totalCount());
		
		if(totalCount()%pageDTO.getPerPage()==0) {
			pageDTO.setPageNum(totalCount()/pageDTO.getPerPage());
		}else {
			pageDTO.setPageNum(totalCount()/pageDTO.getPerPage()+1);
		}
		
		return pageDTO;
	}
	
	public int acceptAdd(AcceptDTO dto) {
		return session.insert("AcceptMapper.acceptAdd", dto);
	}
}
