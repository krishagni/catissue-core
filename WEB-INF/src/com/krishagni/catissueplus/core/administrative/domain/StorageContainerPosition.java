package com.krishagni.catissueplus.core.administrative.domain;

import java.util.Date;

import org.hibernate.envers.Audited;
import org.springframework.beans.BeanUtils;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;

@Audited
public class StorageContainerPosition implements Comparable<StorageContainerPosition> {
	private Long id;
	
	private Integer posOneOrdinal;
	
	private Integer posTwoOrdinal;
	
	private String posOne;
	
	private String posTwo;
	
	private StorageContainer container;
	
	private Specimen occupyingSpecimen;
	
	private StorageContainer occupyingContainer;

	private String reservationId;

	private Date reservationTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getPosOneOrdinal() {
		return posOneOrdinal;
	}

	public void setPosOneOrdinal(Integer posOneOrdinal) {
		this.posOneOrdinal = posOneOrdinal;
	}

	public Integer getPosTwoOrdinal() {
		return posTwoOrdinal;
	}

	public void setPosTwoOrdinal(Integer posTwoOrdinal) {
		this.posTwoOrdinal = posTwoOrdinal;
	}

	public String getPosOne() {
		return posOne;
	}

	public void setPosOne(String posOne) {
		this.posOne = posOne;
	}

	public String getPosTwo() {
		return posTwo;
	}

	public void setPosTwo(String posTwo) {
		this.posTwo = posTwo;
	}

	public Integer getPosition() {
		if (getContainer().isDimensionless()) {
			return null;
		}

		return (getPosTwoOrdinal() - 1) * getContainer().getNoOfColumns() + getPosOneOrdinal();
	}

	public StorageContainer getContainer() {
		return container;
	}

	public Specimen getOccupyingSpecimen() {
		return occupyingSpecimen;
	}

	public void setOccupyingSpecimen(Specimen occupyingSpecimen) {
		this.occupyingSpecimen = occupyingSpecimen;
	}

	public StorageContainer getOccupyingContainer() {
		return occupyingContainer;
	}

	public void setOccupyingContainer(StorageContainer occupyingContainer) {
		this.occupyingContainer = occupyingContainer;
	}

	public void setContainer(StorageContainer container) {
		this.container = container;
	}

	public String getReservationId() {
		return reservationId;
	}

	public void setReservationId(String reservationId) {
		this.reservationId = reservationId;
	}

	public Date getReservationTime() {
		return reservationTime;
	}

	public void setReservationTime(Date reservationTime) {
		this.reservationTime = reservationTime;
	}

	public void update(StorageContainerPosition other) {
		//
		// Ideally when container changes, we should first remove it from old container
		// map and then add to new container map. However this creates problem because
		// Hibernates does all inserts first, followed by updates and then delete
		// So added position in new container is deleted by remove from old container 
		// Therefore the hack is to only the new position and not remove it from old
		// container
		//
		
		boolean isContainerChanged = !getContainer().equals(other.getContainer());
		BeanUtils.copyProperties(other, this, POS_UPDATE_IGN_PROPS);
		
		if (isContainerChanged) { // The old position is not freed because hibernate deletes it 
			occupy();
		}
	}
	
	public void occupy() {
		container.addPosition(this);
	}
	
	public void vacate() {
		container.removePosition(this);
	}

	public boolean equals(String row, String column, String reservationId) {
		return row.equals(getPosTwo()) && column.equals(getPosOne()) && reservationId.equals(getReservationId());
	}
	
	private static final String[] POS_UPDATE_IGN_PROPS = new String[] {
		"id", 
		"occupyingSpecimen", 
		"occupyingContainer"
	};

	@Override
	public int compareTo(StorageContainerPosition other) {
		int cmp;
		if (getContainer().isDimensionless()) {
			cmp = getId().compareTo(other.getId());
		} else {
			cmp = getPosTwoOrdinal().compareTo(other.getPosTwoOrdinal());
			if (cmp == 0) {
				cmp = getPosOneOrdinal().compareTo(other.getPosOneOrdinal());
			}
		}

		return cmp;
	}
}
