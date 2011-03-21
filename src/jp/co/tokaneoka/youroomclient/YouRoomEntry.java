package jp.co.tokaneoka.youroomclient;

import java.io.Serializable;

// ListView�J�X�^�}�C�Y�p��ArrayAdapter�ɗ��p����N���X    
public class YouRoomEntry implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id;
	private String content;
	private int rootId;
	private int parentId;
	private String createdTime;
	private String updatedTime;
	private int descendantsCount;
	private String participationName;
	private String participationId;
	
	public int getDescendantsCount() {
		return descendantsCount;
	}
	public void setDescendantsCount(int descendantsCount) {
		this.descendantsCount = descendantsCount;
	}
			
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getRootId() {
		return rootId;
	}
	public void setRootId(int rootId) {
		this.rootId = rootId;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	public String getUpdatedTime() {
		return updatedTime;
	}
	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}
	public String getParticipationName() {
		return participationName;
	}
	public void setParticipationName(String participationName) {
		this.participationName = participationName;
	}
	public String getParticipationId() {
		return participationId;
	}
	public void setParticipationId(String participationId) {
		this.participationId = participationId;
	}

}