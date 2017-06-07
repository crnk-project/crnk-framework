package io.crnk.gen.typescript.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class TSObjectType extends TSTypeBase implements TSExportedElement {

	private List<TSMember> declaredMembers = new ArrayList<>();

	private Set<TSInterfaceType> implementedInterfaces = new HashSet<>();

	private boolean exported;

	private TSIndexSignature indexSignature;

	public List<TSMember> getDeclaredMembers() {
		return declaredMembers;
	}

	public void addDeclaredMember(TSMember member) {
		declaredMembers.add(member);
		member.setParent(this);
	}

	public TSMember getDeclaredMember(String name) {
		for (TSMember member : declaredMembers) {
			if (name.equals(member.getName())) {
				return member;
			}
		}
		return null;
	}

	public List<TSMember> getMembers() {
		List<TSMember> members = new ArrayList<>();

		Map<String, TSMember> memberMap = new HashMap<>();

		for (TSMember member : declaredMembers) {
			members.add(member);
			memberMap.put(member.getName(), member);
		}

		for (TSInterfaceType implementedInterface : implementedInterfaces) {
			List<TSMember> implementedMembers = implementedInterface.getMembers();
			for (TSMember implementedMember : implementedMembers) {
				if (memberMap.containsKey(implementedMember.getName())) {
					continue;
				}
				members.add(implementedMember);
				memberMap.put(implementedMember.getName(), implementedMember);
			}
		}

		return declaredMembers;
	}

	public void setDeclaredMembers(List<TSMember> members) {
		this.declaredMembers = members;
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
		for (TSMember member : declaredMembers) {
			if (member instanceof TSField) {
				fields.add((TSField) member);
			}
		}
		return fields;
	}

}
