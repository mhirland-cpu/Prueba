const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const CHARLIE_UID = "charlie_uid_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function makeDaySchedule(overrides = {}) {
  return {
    salidaAmLugar: "Casa Fam. Pérez",
    salidaAmHora: "8:30 am",
    salidaPmLugar: "Salón del Reino",
    salidaPmHora: "3:00 pm",
    almuerzoFamilia: "Familia Almonte",
    almuerzoTel: "849-203-1217",
    reunionPrecursoresEnabled: false,
    reunionPrecursoresLugar: "Salón del Reino",
    reunionPrecursoresHora: "11:30 am",
    ...overrides,
  };
}

function makeActividades(overrides = {}) {
  return {
    fecha: "10 al 15 de noviembre de 2026",
    congregacion: "Villa Francisca",
    miercoles: makeDaySchedule({ reunionPrecursoresEnabled: true }),
    jueves: makeDaySchedule(),
    viernes: makeDaySchedule(),
    reunionAncianosLugar: "Salón del Reino",
    reunionAncianosHora: "7:00 pm",
    sabado: makeDaySchedule(),
    domingoSalidaAmLugar: "Salón del Reino",
    domingoSalidaAmHora: "8:30 am",
    domingoAlmuerzoFamilia: "Familia Minaya",
    domingoAlmuerzoTel: "829-810-4097",
    domingoReunionPrecursoresEnabled: false,
    domingoReunionPrecursoresLugar: "Salón del Reino",
    domingoReunionPrecursoresHora: "11:30 am",
    reunionVidaMinisterioDia: "Martes",
    reunionVidaMinisterioHora: "7:00 pm",
    reunionDiscursoAtalayaDia: "Sábado",
    reunionDiscursoAtalayaHora: "6:00 pm",
    ...overrides,
  };
}

function makePastoringDay(overrides = {}) {
  return {
    hora1Con: "Benjamín Herrera",
    hora1Tel: "(809) 919-4150",
    hora1EstudioPastoreo: "Estudio bíblico",
    hora1Publicacion: "Disfrute de la vida",
    hora2Con: "",
    hora2Tel: "",
    hora2EstudioPastoreo: "",
    hora2Publicacion: "",
    ...overrides,
  };
}

function makeServicioPastoreo(overrides = {}) {
  return {
    nombrePersona: "Obed Paulino",
    miercoles: makePastoringDay(),
    jueves: makePastoringDay({ hora1Con: "Misael Hirland" }),
    viernes: makePastoringDay({ hora1Con: "" }),
    notasAdicionales: "",
    ...overrides,
  };
}

function makeServiceOnlyDay(overrides = {}) {
  return {
    hora1Con: "Ana López",
    hora1Tel: "(809) 555-0101",
    hora1Publicacion: "Folleto",
    hora2Con: "",
    hora2Tel: "",
    hora2Publicacion: "",
    ...overrides,
  };
}

function makeServicioEsposa(overrides = {}) {
  return {
    nombrePersona: "Mercedes Paulino",
    miercoles: makeServiceOnlyDay(),
    jueves: makeServiceOnlyDay({ hora1Con: "" }),
    viernes: makeServiceOnlyDay({ hora1Con: "" }),
    notasAdicionales: "",
    ...overrides,
  };
}

function makeNotaPastoreo(overrides = {}) {
  return {
    visitaMiercolesPersona: "Sergia Viera",
    visitaMiercolesSituacion: "Hermana mayor con quebrantos de salud.",
    visitaJuevesPersona: "",
    visitaJuevesSituacion: "",
    visitaViernesPersona: "",
    visitaViernesSituacion: "",
    asunto1: "Revisar territorios",
    asunto2: "",
    asunto3: "",
    asunto4: "",
    codigoPie: "Ago/26",
    ...overrides,
  };
}

function makeParentPayload(ownerUid = ALICE_UID, ownerName = "Obed Paulino", timestamp = new Date(Date.now() - 5000)) {
  return {
    ownerId: ownerUid,
    ownerName,
    memberUids: [ownerUid],
    memberNames: { [ownerUid]: ownerName },
    congregacion: "Villa Francisca",
    fechaVisita: "10 al 15 de noviembre de 2026",
    lastEditedByUid: ownerUid,
    lastEditedByName: ownerName,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

async function createSharedArregloBatch(db, code, ownerUid = ALICE_UID, ownerName = "Obed Paulino", timestamp = new Date(Date.now() - 5000)) {
  const batch = db.batch();
  const parentRef = db.collection("shared_arreglos").doc(code);
  batch.set(parentRef, makeParentPayload(ownerUid, ownerName, timestamp));
  batch.set(parentRef.collection("sections").doc("actividades"), {
    sectionType: "actividades",
    lastEditedByUid: ownerUid,
    lastEditedByName: ownerName,
    payload: makeActividades(),
    updatedAt: timestamp,
  });
  batch.set(parentRef.collection("sections").doc("servicioPastoreo"), {
    sectionType: "servicioPastoreo",
    lastEditedByUid: ownerUid,
    lastEditedByName: ownerName,
    payload: makeServicioPastoreo(),
    updatedAt: timestamp,
  });
  batch.set(parentRef.collection("sections").doc("servicioEsposa"), {
    sectionType: "servicioEsposa",
    lastEditedByUid: ownerUid,
    lastEditedByName: ownerName,
    payload: makeServicioEsposa(),
    updatedAt: timestamp,
  });
  batch.set(parentRef.collection("sections").doc("notaPastoreo"), {
    sectionType: "notaPastoreo",
    lastEditedByUid: ownerUid,
    lastEditedByName: ownerName,
    payload: makeNotaPastoreo(),
    updatedAt: timestamp,
  });
  return batch.commit();
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read, list, or create shared_arreglos or sections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("shared_arreglos").get());
  await assertFails(unauthDb.collection("shared_arreglos").doc("AR7K4M9P").get());
  await assertFails(unauthDb.collection("shared_arreglos").doc("AR7K4M9P").collection("sections").doc("actividades").get());
  await assertFails(
    unauthDb.collection("shared_arreglos").doc("AR7K4M9P").set(makeParentPayload())
  );
});

test("Owner creates atomic batch, collaborator joins via code, edits separate sections concurrently, and queries list", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  const code = "AR7K4M9P";
  await assertSucceeds(createSharedArregloBatch(aliceDb, code, ALICE_UID, "Obed Paulino"));

  // Non-member Charlie cannot read parent or sections before joining
  await assertFails(charlieDb.collection("shared_arreglos").doc(code).get());
  await assertFails(charlieDb.collection("shared_arreglos").doc(code).collection("sections").doc("actividades").get());

  // Unfiltered list query & sections collection list must fail
  await assertFails(aliceDb.collection("shared_arreglos").get());
  await assertFails(aliceDb.collection("shared_arreglos").doc(code).collection("sections").get());

  // Filtered query matching memberUids succeeds for Alice
  await assertSucceeds(
    aliceDb.collection("shared_arreglos").where("memberUids", "array-contains", ALICE_UID).get()
  );

  // Bob joins using the share code
  await assertSucceeds(
    bobDb.collection("shared_arreglos").doc(code).update({
      memberUids: [ALICE_UID, BOB_UID],
      [`memberNames.${BOB_UID}`]: "Misael Hirland",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Now Bob can get parent & all 4 sections and list shared_arreglos
  await assertSucceeds(bobDb.collection("shared_arreglos").doc(code).get());
  await assertSucceeds(bobDb.collection("shared_arreglos").doc(code).collection("sections").doc("actividades").get());
  await assertSucceeds(bobDb.collection("shared_arreglos").doc(code).collection("sections").doc("notaPastoreo").get());
  await assertSucceeds(
    bobDb.collection("shared_arreglos").where("memberUids", "array-contains", BOB_UID).get()
  );

  // Alice updates section 1 (actividades) + parent summary metadata in a batch
  const aliceBatch = aliceDb.batch();
  aliceBatch.update(aliceDb.collection("shared_arreglos").doc(code).collection("sections").doc("actividades"), {
    payload: makeActividades({ congregacion: "Los Mina" }),
    lastEditedByUid: ALICE_UID,
    lastEditedByName: "Obed Paulino",
    updatedAt: new Date(Date.now() - 500),
  });
  aliceBatch.update(aliceDb.collection("shared_arreglos").doc(code), {
    congregacion: "Los Mina",
    fechaVisita: "17 al 22 de noviembre de 2026",
    lastEditedByUid: ALICE_UID,
    lastEditedByName: "Obed Paulino",
    updatedAt: new Date(Date.now() - 500),
  });
  await assertSucceeds(aliceBatch.commit());

  // Bob updates section 4 (notaPastoreo) without touching actividades
  await assertSucceeds(
    bobDb.collection("shared_arreglos").doc(code).collection("sections").doc("notaPastoreo").update({
      payload: makeNotaPastoreo({ asunto2: "Coordinar predicación pública" }),
      lastEditedByUid: BOB_UID,
      lastEditedByName: "Misael Hirland",
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Bob updates section 2 (servicioPastoreo) & section 3 (servicioEsposa)
  await assertSucceeds(
    bobDb.collection("shared_arreglos").doc(code).collection("sections").doc("servicioPastoreo").update({
      payload: makeServicioPastoreo({ notasAdicionales: "Confirmado" }),
      lastEditedByUid: BOB_UID,
      lastEditedByName: "Misael Hirland",
      updatedAt: new Date(Date.now() - 100),
    })
  );
  await assertSucceeds(
    bobDb.collection("shared_arreglos").doc(code).collection("sections").doc("servicioEsposa").update({
      payload: makeServicioEsposa({ notasAdicionales: "Revisado" }),
      lastEditedByUid: BOB_UID,
      lastEditedByName: "Misael Hirland",
      updatedAt: new Date(Date.now() - 50),
    })
  );

  // Bob cannot delete Alice's shared arreglo or sections; Bob can leave; Alice can delete
  await assertFails(bobDb.collection("shared_arreglos").doc(code).delete());
  await assertFails(bobDb.collection("shared_arreglos").doc(code).collection("sections").doc("actividades").delete());
  await assertSucceeds(bobDb.collection("shared_arreglos").doc(code).update({
    memberUids: [ALICE_UID],
    updatedAt: new Date(Date.now() - 20),
  }));
  const deleteBatch = aliceDb.batch();
  for (const sec of ["actividades", "servicioPastoreo", "servicioEsposa", "notaPastoreo"]) {
    deleteBatch.delete(aliceDb.collection("shared_arreglos").doc(code).collection("sections").doc(sec));
  }
  deleteBatch.delete(aliceDb.collection("shared_arreglos").doc(code));
  await assertSucceeds(deleteBatch.commit());
});

test("Rejects orphan creation, spoofing, shadow ghost fields, immutable mutation, future timestamps, and oversized strings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const code = "VK8M3P9Q";

  // Reject creating parent alone without atomic sections
  await assertFails(aliceDb.collection("shared_arreglos").doc(code).set(makeParentPayload(ALICE_UID, "Obed")));

  // Reject creating with extra @DocumentId 'id' field
  const withId = makeParentPayload(ALICE_UID, "Obed");
  withId.id = code;
  await assertFails(aliceDb.collection("shared_arreglos").doc(code).set(withId));

  // Reject creating with someone else's ownerId
  await assertFails(createSharedArregloBatch(aliceDb, code, BOB_UID, "Bob"));

  // Reject future timestamp
  await assertFails(createSharedArregloBatch(aliceDb, code, ALICE_UID, "Obed", new Date(Date.now() + 60000)));

  // Valid atomic create
  await assertSucceeds(createSharedArregloBatch(aliceDb, code, ALICE_UID, "Obed"));

  // Reject shadow/ghost field on parent update and section update
  await assertFails(
    aliceDb.collection("shared_arreglos").doc(code).update({
      isAdmin: true,
      lastEditedByUid: ALICE_UID,
      lastEditedByName: "Obed",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("shared_arreglos").doc(code).collection("sections").doc("notaPastoreo").update({
      payload: { ...makeNotaPastoreo(), ghostField: "x" },
      lastEditedByUid: ALICE_UID,
      lastEditedByName: "Obed",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable ownerId or createdAt modification
  await assertFails(
    aliceDb.collection("shared_arreglos").doc(code).update({
      ownerId: BOB_UID,
      lastEditedByUid: ALICE_UID,
      lastEditedByName: "Obed",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("shared_arreglos").doc(code).update({
      createdAt: new Date(Date.now() - 100),
      lastEditedByUid: ALICE_UID,
      lastEditedByName: "Obed",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject Bob writing to sections without joining first
  await assertFails(
    bobDb.collection("shared_arreglos").doc(code).collection("sections").doc("actividades").update({
      payload: makeActividades({ congregacion: "Hackeado" }),
      lastEditedByUid: BOB_UID,
      lastEditedByName: "Bob",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject oversized string (value poisoning)
  await assertFails(
    aliceDb.collection("shared_arreglos").doc(code).collection("sections").doc("notaPastoreo").update({
      payload: makeNotaPastoreo({ asunto1: "x".repeat(2000) }),
      lastEditedByUid: ALICE_UID,
      lastEditedByName: "Obed",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
