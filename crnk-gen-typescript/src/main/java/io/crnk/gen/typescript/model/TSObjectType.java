package io.crnk.gen.typescript.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TSObjectType extends TSTypeBase implements TSExportedElement {

	private List<TSMember> members = new ArrayList<>();

	private Set<TSInterfaceType> implementedInterfaces = new HashSet<>();

	private boolean exported;

	private TSIndexSignature indexSignature;

	public List<TSMember> getMembers() {
		return members;
	}

	public void addMember(TSMember member) {
		members.add(member);
		member.setParent(this);
	}

	public void setMembers(List<TSMember> members) {
		this.members = members;
	}

	public Set<TSInterfaceType> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	public void setImplementedInterfaces(Set<TSInterfaceType> implementedInterfaces) {
		this.implementedInterfaces = implementedInterfaces;
	}

	@Override
	public boolean isExported() {
		return exported;
	}

	public void setExported(boolean exported) {
		this.exported = exported;
	}

	public TSIndexSignature getIndexSignature() {
		return indexSignature;
	}

	public void setIndexSignature(TSIndexSignature indexSignature) {
		this.indexSignature = indexSignature;
	}

	public List<TSField> getFields() {
		List<TSField> fields = new ArrayList<>();
		for (TSMember member : members) {
			if (member instanceof TSField) {
				fields.add((TSField) member);
			}
		}
		return fields;
	}

}
