describe("Ext.data.schema.Namer",function(){var E,C,B,I,H,J,A,D,F,G;beforeEach(function(){D=Ext.data.Model.schema;F=[];G=[];D.setNamespace("spec.data.namer");E=Ext.define("spec.data.namer.Base",{extend:"Ext.data.Model",schema:{namespace:"spec.data.namer"}});C=Ext.define("spec.data.namer.Company",{extend:"spec.data.namer.Base",fields:["name"]});B=Ext.define("spec.data.namer.Department",{extend:"spec.data.namer.Base",fields:["name",{name:"companyId",reference:"Company"},{name:"managerId",reference:"User",unique:true}],manyToMany:[{type:"User",relation:"approved"},{type:"User",relation:"qualified"}]});I=Ext.define("spec.data.namer.User",{extend:"spec.data.namer.Base",fields:["name",{name:"companyId",reference:"Company"},{name:"departmentId",reference:"Department"}],manyToMany:"#Group"});H=Ext.define("spec.data.namer.Group",{extend:"spec.data.namer.Base",fields:["name"],manyToMany:"User#"});J=Ext.define("spec.data.namer.Ticket",{extend:"spec.data.namer.Base",fields:["description",{name:"creatorId",reference:"User"},{name:"assigneeId",reference:"User"}]});A=Ext.define("spec.data.namer.Comment",{extend:"spec.data.namer.Base",fields:["name",{name:"ticketId",reference:{parent:"Ticket"}},{name:"userId",reference:"User"}]});D.eachAssociation(function(K){F.push(K)});D.eachEntity(function(K){G.push(K)});F.sort();G.sort()});afterEach(function(){Ext.Array.forEach(G,function(K){Ext.undefine("spec.data.namer."+K)});D.setNamespace(null);D.clear();E=C=B=I=H=J=A=D=F=G=null});describe("Schema",function(){it("should have the right number of associations",function(){expect(F.length).toBe(11)});it("should have the right number of entities",function(){expect(G.length).toBe(7)})});describe("Company",function(){describe("departments",function(){it("should have the association",function(){expect(C.associations.departments.isRole).toBe(true)});it("should properly name the association",function(){expect(C.associations.departments.association.name).toBe("CompanyDepartments")});it("association kind should be many-to-one",function(){expect(C.associations.departments.association.kind).toBe("many-to-one")});it("association has Company on the right",function(){expect(C.associations.departments.association.right.type).toBe("Company")});it("association should refer to target type",function(){expect(C.associations.departments.cls).toBe(B);expect(C.associations.departments.type).toBe("Department")});it("should have a getter",function(){expect(typeof C.prototype.departments).toBe("function")});it("should not have a setter",function(){expect(C.prototype.setDepartments).toBe(undefined)})});describe("users",function(){it("should have the association",function(){expect(C.associations.users.isRole).toBe(true)});it("should properly name the users association",function(){expect(C.associations.users.association.name).toBe("CompanyUsers")});it("users association should be many-to-one",function(){expect(C.associations.users.association.kind).toBe("many-to-one")});it("users has Company on the right",function(){expect(C.associations.users.association.right.type).toBe("Company")});it("users association should refer to User",function(){expect(C.associations.users.cls).toBe(I);expect(C.associations.users.type).toBe("User")});it("should have a users getter",function(){expect(typeof C.prototype.users).toBe("function")});it("should not have a departments setter",function(){expect(C.prototype.setUsers).toBe(undefined)})})});describe("Department",function(){describe("company",function(){it("should have the association",function(){expect(B.associations.company.isRole).toBe(true)});it("should properly name the company association",function(){expect(B.associations.company.association.name).toBe("CompanyDepartments")});it("company association should be many-to-one",function(){expect(B.associations.company.association.kind).toBe("many-to-one")});it("company has Department on the left",function(){expect(B.associations.company.association.left.type).toBe("Department")});it("company association should refer to User",function(){expect(B.associations.company.cls).toBe(C);expect(B.associations.company.type).toBe("Company")});it("should have an company getter",function(){expect(typeof B.prototype.getCompany).toBe("function")});it("should have an company setter",function(){expect(typeof B.prototype.setCompany).toBe("function")})});describe("approvedUsers",function(){it("should have the association",function(){expect(B.associations.approvedUsers.isRole).toBe(true)});it("should properly name the approvedUsers association",function(){expect(B.associations.approvedUsers.association.name).toBe("ApprovedDepartmentUsers")});it("approvedUsers association should be many-to-many",function(){expect(B.associations.approvedUsers.association.kind).toBe("many-to-many")});it("approvedUsers has Department on the left",function(){expect(B.associations.approvedUsers.association.left.type).toBe("Department")});it("approvedUsers association should refer to User",function(){expect(B.associations.approvedUsers.cls).toBe(I);expect(B.associations.approvedUsers.type).toBe("User")});it("should have an approvedUsers getter",function(){expect(typeof B.prototype.approvedUsers).toBe("function")});it("should not have an approvedUsers setter",function(){expect(B.prototype.setApprovedUsers).toBe(undefined)})});describe("qualifiedUsers",function(){it("should have the association",function(){expect(B.associations.qualifiedUsers.isRole).toBe(true)});it("should properly name the qualifiedUsers association",function(){expect(B.associations.qualifiedUsers.association.name).toBe("QualifiedDepartmentUsers")});it("qualifiedUsers association should be many-to-many",function(){expect(B.associations.qualifiedUsers.association.kind).toBe("many-to-many")});it("qualifiedUsers has Department on the left",function(){expect(B.associations.qualifiedUsers.association.left.type).toBe("Department")});it("qualifiedUsers association should refer to User",function(){expect(B.associations.qualifiedUsers.cls).toBe(I);expect(B.associations.qualifiedUsers.type).toBe("User")});it("should have a qualifiedUsers getter",function(){expect(typeof B.prototype.qualifiedUsers).toBe("function")});it("should not have a qualifiedUsers setter",function(){expect(B.prototype.setQualifiedUsers).toBe(undefined)})});describe("manager",function(){it("should have the association",function(){expect(B.associations.manager.isRole).toBe(true)});it("should properly name the manager association",function(){expect(B.associations.manager.association.name).toBe("UserManagerDepartment")});it("association should be one-to-one",function(){expect(B.associations.manager.association.kind).toBe("one-to-one")});it("manager has Department on the left",function(){expect(B.associations.manager.association.left.type).toBe("Department")});it("manager association should refer to User",function(){expect(B.associations.manager.cls).toBe(I);expect(B.associations.manager.type).toBe("User")});it("should have a manager getter",function(){expect(typeof B.prototype.getManager).toBe("function")});it("should have a manager setter",function(){expect(typeof B.prototype.setManager).toBe("function")})});describe("users",function(){it("should have the association",function(){expect(B.associations.users.isRole).toBe(true)});it("should properly name the users association",function(){expect(B.associations.users.association.name).toBe("DepartmentUsers")});it("association should be many-to-one",function(){expect(B.associations.users.association.kind).toBe("many-to-one")});it("users has Department on the right",function(){expect(B.associations.users.association.right.type).toBe("Department")});it("users association should refer to User",function(){expect(B.associations.users.cls).toBe(I);expect(B.associations.users.type).toBe("User")});it("should have a users getter",function(){expect(typeof B.prototype.users).toBe("function")});it("should not have a users setter",function(){expect(B.prototype.setUsers).toBe(undefined)})})});describe("User",function(){describe("company",function(){it("should have the association",function(){expect(I.associations.company.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.company.association.name).toBe("CompanyUsers")});it("association should be many-to-one",function(){expect(I.associations.company.association.kind).toBe("many-to-one")});it("should have User on the left",function(){expect(I.associations.company.association.left.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.company.cls).toBe(C);expect(I.associations.company.type).toBe("Company")});it("should have a getter",function(){expect(typeof I.prototype.getCompany).toBe("function")});it("should have a setter",function(){expect(typeof I.prototype.setCompany).toBe("function")})});describe("assigneeTickets",function(){it("should have the association",function(){expect(I.associations.assigneeTickets.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.assigneeTickets.association.name).toBe("UserAssigneeTickets")});it("association should be many-to-one",function(){expect(I.associations.assigneeTickets.association.kind).toBe("many-to-one")});it("should have User on the right",function(){expect(I.associations.assigneeTickets.association.right.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.assigneeTickets.cls).toBe(J);expect(I.associations.assigneeTickets.type).toBe("Ticket")});it("should have a getter",function(){expect(typeof I.prototype.assigneeTickets).toBe("function")});it("should not have a setter",function(){expect(typeof I.prototype.setAssigneeTickets).toBe("undefined")})});describe("creatorTickets",function(){it("should have the association",function(){expect(I.associations.creatorTickets.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.creatorTickets.association.name).toBe("UserCreatorTickets")});it("association should be many-to-one",function(){expect(I.associations.creatorTickets.association.kind).toBe("many-to-one")});it("should have User on the right",function(){expect(I.associations.creatorTickets.association.right.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.creatorTickets.cls).toBe(J);expect(I.associations.creatorTickets.type).toBe("Ticket")});it("should have a getter",function(){expect(typeof I.prototype.creatorTickets).toBe("function")});it("should not have a setter",function(){expect(typeof I.prototype.setCreatorTickets).toBe("undefined")})});describe("department",function(){it("should have the association",function(){expect(I.associations.department.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.department.association.name).toBe("DepartmentUsers")});it("association should be many-to-one",function(){expect(I.associations.department.association.kind).toBe("many-to-one")});it("should have User on the left",function(){expect(I.associations.department.association.left.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.department.cls).toBe(B);expect(I.associations.department.type).toBe("Department")});it("should have a getter",function(){expect(typeof I.prototype.getDepartment).toBe("function")});it("should have a setter",function(){expect(typeof I.prototype.setDepartment).toBe("function")})});describe("groups",function(){it("should have the association",function(){expect(I.associations.groups.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.groups.association.name).toBe("UserGroups")});it("association should be many-to-many",function(){expect(I.associations.groups.association.kind).toBe("many-to-many")});it("should have User on the left",function(){expect(I.associations.groups.association.left.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.groups.cls).toBe(H);expect(I.associations.groups.type).toBe("Group")});it("should have a getter",function(){expect(typeof I.prototype.groups).toBe("function")});it("should not have a setter",function(){expect(typeof I.prototype.setGroups).toBe("undefined")})});describe("approvedDepartments",function(){it("should have the association",function(){expect(I.associations.approvedDepartments.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.approvedDepartments.association.name).toBe("ApprovedDepartmentUsers")});it("association should be many-to-many",function(){expect(I.associations.approvedDepartments.association.kind).toBe("many-to-many")});it("should have User on the right",function(){expect(I.associations.approvedDepartments.association.right.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.approvedDepartments.cls).toBe(B);expect(I.associations.approvedDepartments.type).toBe("Department")});it("should have a getter",function(){expect(typeof I.prototype.approvedDepartments).toBe("function")});it("should not have a setter",function(){expect(typeof I.prototype.setApprovedDepartments).toBe("undefined")})});describe("qualifiedDepartments",function(){it("should have the association",function(){expect(I.associations.qualifiedDepartments.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.approvedDepartments.association.name).toBe("ApprovedDepartmentUsers")});it("association should be many-to-many",function(){expect(I.associations.qualifiedDepartments.association.kind).toBe("many-to-many")});it("should have User on the right",function(){expect(I.associations.qualifiedDepartments.association.right.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.qualifiedDepartments.cls).toBe(B);expect(I.associations.qualifiedDepartments.type).toBe("Department")});it("should have a getter",function(){expect(typeof I.prototype.qualifiedDepartments).toBe("function")});it("should not have a setter",function(){expect(typeof I.prototype.setQualifiedDepartments).toBe("undefined")})});describe("managerDepartment",function(){it("should have the association",function(){expect(I.associations.managerDepartment.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.managerDepartment.association.name).toBe("UserManagerDepartment")});it("association should be one-to-one",function(){expect(I.associations.managerDepartment.association.kind).toBe("one-to-one")});it("should have User on the right",function(){expect(I.associations.managerDepartment.association.right.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.managerDepartment.cls).toBe(B);expect(I.associations.managerDepartment.type).toBe("Department")});it("should have a getter",function(){expect(typeof I.prototype.getManagerDepartment).toBe("function")});it("should have a setter",function(){expect(typeof I.prototype.setManagerDepartment).toBe("function")})});describe("comments",function(){it("should have the association",function(){expect(I.associations.comments.isRole).toBe(true)});it("should properly name the association",function(){expect(I.associations.comments.association.name).toBe("UserComments")});it("association should be many-to-one",function(){expect(I.associations.comments.association.kind).toBe("many-to-one")});it("should have User on the right",function(){expect(I.associations.comments.association.right.type).toBe("User")});it("association should refer to proper entity",function(){expect(I.associations.comments.cls).toBe(A);expect(I.associations.comments.type).toBe("Comment")});it("should have a getter",function(){expect(typeof I.prototype.comments).toBe("function")});it("should not have a setter",function(){expect(typeof I.prototype.setComments).toBe("undefined")})})});describe("Group",function(){describe("users",function(){it("should have the association",function(){expect(H.associations.users.isRole).toBe(true)});it("should properly name the association",function(){expect(H.associations.users.association.name).toBe("UserGroups")});it("association should be many-to-many",function(){expect(H.associations.users.association.kind).toBe("many-to-many")});it("should have Group on the right",function(){expect(H.associations.users.association.right.type).toBe("Group")});it("association should refer to proper entity",function(){expect(H.associations.users.cls).toBe(I);expect(H.associations.users.type).toBe("User")});it("should have a getter",function(){expect(typeof H.prototype.users).toBe("function")});it("should not have a setter",function(){expect(typeof H.prototype.setUsers).toBe("undefined")})})});describe("Ticket",function(){describe("comments",function(){it("should have the association",function(){expect(J.associations.comments.isRole).toBe(true)});it("should properly name the association",function(){expect(J.associations.comments.association.name).toBe("TicketComments")});it("association should be many-to-one",function(){expect(J.associations.comments.association.kind).toBe("many-to-one")});it("should have the proper side",function(){expect(J.associations.comments.association.right.type).toBe("Ticket")});it("association should refer to proper entity",function(){expect(J.associations.comments.cls).toBe(A);expect(J.associations.comments.type).toBe("Comment")});it("should have a getter",function(){expect(typeof J.prototype.comments).toBe("function")});it("should not have a setter",function(){expect(typeof J.prototype.setComments).toBe("undefined")})});describe("assignee",function(){it("should have the association",function(){expect(J.associations.assignee.isRole).toBe(true)});it("should properly name the association",function(){expect(J.associations.assignee.association.name).toBe("UserAssigneeTickets")});it("association should be many-to-one",function(){expect(J.associations.assignee.association.kind).toBe("many-to-one")});it("should have the proper side",function(){expect(J.associations.assignee.association.left.type).toBe("Ticket")});it("association should refer to proper entity",function(){expect(J.associations.assignee.cls).toBe(I);expect(J.associations.assignee.type).toBe("User")});it("should have a getter",function(){expect(typeof J.prototype.getAssignee).toBe("function")});it("should have a setter",function(){expect(typeof J.prototype.setAssignee).toBe("function")})});describe("creator",function(){it("should have the association",function(){expect(J.associations.creator.isRole).toBe(true)});it("should properly name the association",function(){expect(J.associations.creator.association.name).toBe("UserCreatorTickets")});it("association should be many-to-one",function(){expect(J.associations.creator.association.kind).toBe("many-to-one")});it("should have the proper side",function(){expect(J.associations.creator.association.left.type).toBe("Ticket")});it("association should refer to proper entity",function(){expect(J.associations.creator.cls).toBe(I);expect(J.associations.creator.type).toBe("User")});it("should have a getter",function(){expect(typeof J.prototype.getCreator).toBe("function")});it("should have a setter",function(){expect(typeof J.prototype.setCreator).toBe("function")})})});describe("Comment",function(){describe("ticket",function(){it("should have the association",function(){expect(A.associations.ticket.isRole).toBe(true)});it("should properly name the association",function(){expect(A.associations.ticket.association.name).toBe("TicketComments")});it("association should be many-to-one",function(){expect(A.associations.ticket.association.kind).toBe("many-to-one")});it("should have Comment on the left",function(){expect(A.associations.ticket.association.left.type).toBe("Comment")});it("association should refer to proper entity",function(){expect(A.associations.ticket.cls).toBe(J);expect(A.associations.ticket.type).toBe("Ticket")});it("should have a getter",function(){expect(typeof A.prototype.getTicket).toBe("function")});it("should have a setter",function(){expect(typeof A.prototype.setTicket).toBe("function")})});describe("user",function(){it("should have the association",function(){expect(A.associations.user.isRole).toBe(true)});it("should properly name the association",function(){expect(A.associations.user.association.name).toBe("UserComments")});it("association should be many-to-one",function(){expect(A.associations.user.association.kind).toBe("many-to-one")});it("should have Comment on the left",function(){expect(A.associations.user.association.left.type).toBe("Comment")});it("association should refer to proper entity",function(){expect(A.associations.user.cls).toBe(I);expect(A.associations.user.type).toBe("User")});it("should have a getter",function(){expect(typeof A.prototype.getUser).toBe("function")});it("should have a setter",function(){expect(typeof A.prototype.setUser).toBe("function")})})})})