/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipaserver;

/**
 *
 * @author sdanaresh
 */
public class SelectData {

    String selectedState, selectedDistrict, selectedPS,
            selectedFromMM, selectedFromYYYY, selectedToMM, selectedToYYYY;
    public String[] str = new String[10];
    String keyState, keyDistrict, keyPS;

    public String[] select() {
        String selectStringSql[] = new String[63];

//-t015_psstaffcurr----------------------
//INSERT INTO CIPATemp_DB.dbo.t015_psstaffcurr        
//        selectStringSql[0] = "SELECT pol.*  from t015_psstaffcurr pol ";
        selectStringSql[0] = "SELECT location, pis_code, pis_staffname, pis_designation, date_join, place_from, date_relieve, place_to, photograph, pis_role, belt_no, pis_id, pis_passwd, pis_rank, sex, date_birth, parent, parentage, address_perm, address_curr, telephones, emails from t015_psstaffcurr";
 
        
 //-t014_policestationbeat--------------------------------------------------------------------------
//INSERT INTO CIPATemp_DB.dbo.t014_policestationbeat 
//        selectStringSql[1] = "SELECT pol.*  from t014_policestationbeat pol ";
        selectStringSql[1] = "SELECT location, beat_code, beat_name FROM t014_policestationbeat";

//-t1_registration-------
//INSERT INTO CIPATemp_DB.dbo.t1_registration 
//        selectStringSql[2] = "SELECT *  from t1_registration  "
        selectStringSql[2] = "SELECT location, regn_srno, reg_date, reg_type, reg_type_srno, mode_of_info, receive_ps_dt, receive_ps_tm, gd_entry_no, gd_entry_dt, gd_entry_tm,  duty_officer, investigating_officer, desc_facts, freezed, reg_status, read_permission FROM t1_registration"
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + selectedFromMM + "' "
                + "and '" + selectedToYYYY + selectedToMM + "' ";
        //where to_char(reg_date,'YYYYMM') between '200910' and '200911'

//-t201_fir--------------
//INSERT INTO CIPATemp_DB.dbo.t201_fir 
//        selectStringSql[3] = "SELECT fir.*  from t201_fir fir, "
        selectStringSql[3] = "SELECT fir.location, fir.regn_srno, fir.fir_no, fir.occur_from_dt, fir.occur_from_tm, fir.occur_to_dt, fir.occur_to_tm, fir.occur_district, fir.occur_policestation, fir.occur_direction, fir.occur_distance, fir.occur_beat, fir.occur_place, fir.occur_locality, fir.desc_offence, fir.complainant_srno, fir.victim, fir.local_head, fir.property_value, fir.reasonsfordelay, fir.book_no, fir.book_srno from t201_fir fir, "        
                + "t1_registration reg "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and fir.location = reg.location "
                + "and fir.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1 "
                + "and reg.reg_type_srno <> '0' ";
        
//-t311_transfer---------
//INSERT INTO CIPATemp_DB.dbo.t311_transfer 
//        selectStringSql[4] = "SELECT trf.*  from  "
        selectStringSql[4] = "SELECT trf.location, trf.regn_srno, trf.event_sr, trf.transfer_dt, trf.transfer_reason, trf.transfer_type, trf.sent_rc_no, trf.transferto_io, trf.transferto_district, trf.transferto_ps, trf.transferto_agency, trf.transferfrom_io, trf.transferto_agency_desc, trf.orderby_court_code, trf.orderby_court_name from  "
                + "t1_registration reg, "
                + "t311_transfer trf "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and trf.location = reg.location "
                + "and trf.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1 "
                + "and reg.reg_type_srno <> '0' ";
        
//-t101_actsection----------------------
//INSERT INTO CIPATemp_DB.dbo.t101_actsection 
        selectStringSql[5] = "SELECT ac.location, ac.regn_srno, ac.act_code, ac.sec_code, ac.status, ac.original, ac.flag  from  "
                + "t1_registration reg, "
                + "t101_actsection ac "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and ac.location = reg.location "
                + "and ac.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t301_crime----------------------
//INSERT INTO CIPATemp_DB.dbo.t301_crime 
//        selectStringSql[6] = "SELECT cr.*  from  "
        selectStringSql[6] = "SELECT cr.location, cr.regn_srno, cr.major_head, cr.minor_head, cr.methods, cr.conveyance_used, cr.character_assumed, cr.language_dialect, cr.spl_feature, cr.property_type_maj, cr.motive_crime, cr.suspected_gang from  "        
                + "t1_registration reg, "
                + "t301_crime cr "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and cr.location = reg.location "
                + "and cr.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t301b_crime----------------------
//INSERT INTO CIPATemp_DB.dbo.t301b_crime 
//selectStringSql[7] = "SELECT cri.*  from  "
        selectStringSql[7] = "SELECT cri.location, cri.regn_srno, cri.event_sr, cri.visit_dt, cri.visit_tm, cri.place_type_maj, cri.place_type_min, cri.implements_used, cri.offence_brief, cri.event_sr1, cri.fp_chance, cri.fp_agency, cri.witness1_sr, cri.witness2_sr, cri.fp_rep_dt, cri.fp_rep_content, cri.event_sr2, cri.fp_sentto, cri.fp_sentrcno, cri.fp_sentrcdt, cri.fp_sentthru, cri.fp_recdby from  "
                + "t1_registration reg, "
                + "t301b_crime cri "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and cri.location = reg.location "
                + "and cri.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t102_Person----------------------
//INSERT INTO CIPATemp_DB.dbo.t102_Person 
//        selectStringSql[8] = "SELECT per.*  from  "
        selectStringSql[8] = "SELECT per.location, per.person_srno, per.regn_srno, per.person_type, per.person_type_srno, per.crim_srno, per.sex, per.year_birth, per.year_birth1, per.deleted from  "
                + "t1_registration reg, "
                + "t102_Person per "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and per.location = reg.location "
                + "and per.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t102_Person details for staff----------------------
        selectStringSql[9] = "select  per.location, per.person_srno, per.regn_srno ,per.person_type, "
                + "per.person_type_srno, per.crim_srno, per.sex, per.year_birth, per.year_birth1, per.deleted "
                + "from t102_Person per where per.person_srno in (select person_srno from t305_witness "
                + "where regn_srno in (select regn_srno from t1_registration where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and reg_type_srno <> '0') "
                + "and event_sr is not null and person_srno not  in(select per.person_srno from t102_Person per"
                + " where per.regn_srno in (select regn_srno from t1_registration"
                + " where to_char(reg_date,'YYYYMM') between"
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + " and reg_type_srno <> '0') ))";
//-t1021_Personal ----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1021_Personal 
//        selectStringSql[10] = "SELECT perl.*  from  "
                selectStringSql[10] = "SELECT perl.location, perl.person_srno, perl.name, perl.alias, perl.parentage, perl.parent, perl.marital, perl.nationality, perl.passport_no, perl.passport_issue_dt, perl.passport_issue_place, perl.religion, perl.category, perl.caste, perl.caste_tribe, perl.living_status, perl.edu_qualif, perl.occupation, perl.income_group, perl.address, perl.telephone, perl.juris_dist, perl.juris_ps, perl.add_verified_by, perl.national_id, perl.identity_code, perl.photograph, perl.fingerprint, perl.address_perm, perl.juris_beat, perl.add_juris_dist, perl.add_juris_ps, perl.add_juris_beat from  "
                + "t1_registration reg, "
                + "t102_Person per, "
                + "t1021_Personal perl "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and per.location = reg.location "
                + "and per.regn_srno = reg.regn_srno "
                + "and perl.location = per.location "
                + "and perl.person_srno = per.person_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0'";
                
//-t1021_Personal details for police staff ----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1021_Personal 
//        selectStringSql[11] = "SELECT perl.*  from  "
                selectStringSql[11]="SELECT perl.location, perl.person_srno, perl.name, perl.alias, perl.parentage,"
                        + " perl.parent, perl.marital, perl.nationality, perl.passport_no, perl.passport_issue_dt,"
                        + " perl.passport_issue_place, perl.religion, perl.category, perl.caste, perl.caste_tribe, "
                        + "perl.living_status, perl.edu_qualif, perl.occupation,perl.income_group, perl.address, "
                        + "perl.telephone, perl.juris_dist, perl.juris_ps, perl.add_verified_by, perl.national_id, "
                        + "perl.identity_code, perl.photograph, perl.fingerprint, perl.address_perm, perl.juris_beat,"
                        + " perl.add_juris_dist,perl.add_juris_ps, perl.add_juris_beat from t1021_Personal perl "
                        + "where  perl.person_srno in (select person_srno from t305_witness"
                        + " where regn_srno in (select regn_srno from t1_registration"
                        + " where to_char(reg_date,'YYYYMM') between"
                        + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                        + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                        + "and reg_type_srno <> '0') and event_sr is not null "
                        + "and person_srno not  in(select per.person_srno from t102_Person per "
                        + "where per.regn_srno in (select regn_srno from t1_registration where to_char(reg_date,'YYYYMM') between "
                        + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                        + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                        + " and reg_type_srno <> '0') ))";
						                
//INSERT INTO CIPATemp_DB.dbo.t2013_victim 
//        selectStringSql[12] = "SELECT vic.*  from   "
        selectStringSql[12] = "SELECT vic.location, vic.regn_srno, vic.victim_sr, vic.person_srno, vic.victim_type, vic.victim_type_srno, vic.relation, vic.deleted, vic.event_sr  from   "                
                + "t1_registration reg, "
                + "t2013_victim vic "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and vic.location = reg.location "
                + "and vic.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t305_witness----------------------    
//INSERT INTO CIPATemp_DB.dbo.t305_witness 
//        selectStringSql[13] = "SELECT wit.*  from  "
        selectStringSql[13] = "SELECT wit.location, wit.regn_srno, wit.event_sr, wit.person_srno, wit.witness_sr, wit.witness_dt, wit.witness_tm, wit.witness_place, wit.evidence_type, wit.witness_statement from  "
                + "t1_registration reg, "
                + "t305_witness wit "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and wit.location = reg.location "
                + "and wit.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t2011_accused----------------------    
//INSERT INTO CIPATemp_DB.dbo.t2011_accused 
        selectStringSql[14] = "SELECT acc.location, acc.regn_srno, acc.accused_sr, acc.accused_type, acc.person_srno, acc.criminal_no_type, acc.criminal_no, acc.link_reg_type, acc.link_reg_type_srno, acc.event_sr, acc.arrest_srno, acc.deleted, acc.module from  "
                + "t1_registration reg, "
                + "t2011_accused acc "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and acc.location = reg.location "
                + "and acc.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0'; ";
//-t303_arrest----------------------    
//INSERT INTO CIPATemp_DB.dbo.t303_arrest 
        selectStringSql[15] = "SELECT arr.location, arr.arrest_srno, arr.prov_crim_no, arr.regn_srno, arr.event_sr, arr.arrest_type, arr.arrest_dt, arr.arrest_tm, arr.gd_entry_no, arr.gd_entry_dt, arr.gd_entry_tm, arr.arrest_place, arr.arrest_location, arr.court_code, arr.court_name, arr.action_taken, arr.mlc_srno, arr.person_srno, arr.cust_dt, arr.cust_tm, arr.cust_place, arr.custinfo_to, arr.custinfo_to_relation, arr.custinfo_dt, arr.custinfo_tm, arr.custinfo_phone, arr.fp_taken, arr.dossier_made, arr.prof_receiver, arr.witness1_sr, arr.witness2_sr, arr.arrested_by, arr.arrested_acc_phy_condition, arr.mode_info_relation  from  "
                + "t1_registration reg, "
                + "t303_arrest arr "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and arr.location = reg.location "
                + "and arr.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t10221_physical----------------------    
//INSERT INTO CIPATemp_DB.dbo.t10221_physical 
        selectStringSql[16] = "SELECT phy.location, phy.person_srno, phy.height_cm, phy.weight_kg, phy.phy_build, phy.phy_complexion, phy.phy_teeth, phy.phy_hair, phy.phy_eyes, phy.place_burnmark, phy.place_leucoderma, phy.place_mole, phy.place_scar, phy.place_tattoo, phy.idmark_beard, phy.idmark_face, phy.idmark_moustache, phy.idmark_nose, phy.idmark_voice, phy.dresshabit_sex, phy.dresshabit_com, phy.other_feature from  "
                + "t1_registration reg, "
                + "t102_Person per, "
                + "t10221_physical phy "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and per.location = reg.location "
                + "and per.regn_srno = reg.regn_srno "
                + "and phy.location = per.location "
                + "and phy.person_srno = per.person_srno "
                //                + "and reg.reg_type=1 "
                + "and reg.reg_type_srno <> '0' ";
//-t10222_physical----------------------    
//INSERT INTO CIPATemp_DB.dbo.t10222_physical 
        selectStringSql[17] = "SELECT phy1.location, phy1.person_srno, phy1.feature_type, phy1.feature_code from  "
                + "t1_registration reg, "
                + "t102_Person per, "
                + "t10222_physical phy1 "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and per.location = reg.location "
                + "and per.regn_srno = reg.regn_srno "
                + "and phy1.location = per.location "
                + "and phy1.person_srno = per.person_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t103_properties-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t103_properties 
        selectStringSql[18] = "SELECT pro.location, pro.property_srno, pro.regn_srno, pro.property_status, pro.seizure_srno, pro.property_catg, pro.property_type, pro.property_type_srno, pro.property_value, pro.property_desc, pro.belong_to, pro.deleted, pro.perishable, pro.sample from  "
                + "t1_registration reg, "
                + "t103_properties pro "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and pro.location = reg.location "
                + "and pro.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t1031_automobile-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1031_automobile 
        selectStringSql[19] = "SELECT aut.location, aut.property_srno, aut.auto_sr, aut.make, aut.colour, aut.model_year, aut.registration_no, aut.chassis_no, aut.engine_no from "
                + "t1_registration reg, "
                + "t103_properties pro, "
                + "t1031_automobile aut "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and pro.location = reg.location "
                + "and pro.regn_srno = reg.regn_srno "
                + "and aut.location = pro.location "
                + "and aut.property_srno = pro.property_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t304a_seizure-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t304a_seizure 
        selectStringSql[20] = "SELECT seiz.location, seiz.regn_srno, seiz.seizure_srno, seiz.event_sr, seiz.gd_entry_no, seiz.gd_entry_dt, seiz.gd_entry_tm, seiz.seizure_dt, seiz.seizure_tm, seiz.seizure_place, seiz.seizure_district, seiz.seizure_ps, seiz.arrest_srno, seiz.property_value, seiz.property_status, seiz.belong_to, seiz.witness1_sr, seiz.witness2_sr, seiz.seized_by, seiz.seizure_gd_entry_no from  "
                + "t1_registration reg, "
                + "t304a_seizure seiz "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and seiz.location = reg.location "
                + "and seiz.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t1034a_currency-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1034a_currency 
        selectStringSql[21] = "SELECT cur.location, cur.currency_sr, cur.property_srno, cur.currency_design, cur.material, cur.original from  "
                + "t1_registration reg, "
                + "t103_properties pro, "
                + "t1034a_currency cur "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and pro.location = reg.location "
                + "and pro.regn_srno = reg.regn_srno "
                + "and cur.location = pro.location "
                + "and cur.property_srno = pro.property_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t1034b_currency-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1034b_currency 
        selectStringSql[22] = "SELECT curr.location, curr.currency_sr, curr.denom_sr, curr.denomination, curr.pieces, curr.series, curr.serial_from, curr.serial_to from  "
                + "t1_registration reg, "
                + "t103_properties pro, "
                + "t1034a_currency cur, "
                + "t1034b_currency curr "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and pro.location = reg.location "
                + "and pro.regn_srno = reg.regn_srno "
                + "and pro.location = cur.location "
                + "and pro.property_srno = cur.property_srno "
                + "and curr.location = cur.location "
                + "and curr.currency_sr = cur.currency_sr "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t1032_cultural-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1032_cultural 
        selectStringSql[23] = "SELECT cul.location, cul.cultural_sr, cul.property_srno, cul.nomenclature, cul.height_cm, cul.breadth_cm, cul.depth_cm, cul.weight_kg, cul.material, cul.age_bcad, cul.asi_no, cul.sp_feature1, cul.sp_feature2, cul.sp_feature3, cul.photo_collected from  "
                + "t1_registration reg, "
                + "t103_properties pro, "
                + "t1032_cultural cul "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and pro.location = reg.location "
                + "and pro.regn_srno = reg.regn_srno "
                + "and cul.location = pro.location "
                + "and cul.property_srno = pro.property_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t1035_narcotics-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1035_narcotics 
        selectStringSql[24] = "SELECT nar.location, nar.drug_sr, nar.property_srno, nar.concealment, nar.weight_gm, nar.packets, nar.area_operation, nar.area_acres, nar.plants, nar.yield_kg  from  "
                + "t1_registration reg, "
                + "t103_properties pro, "
                + "t1035_narcotics nar "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and pro.location = reg.location "
                + "and pro.regn_srno = reg.regn_srno "
                + "and nar.location = pro.location "
                + "and nar.property_srno = pro.property_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t1033_numbered-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t1033_numbered 
        selectStringSql[25] = "SELECT num.location, num.numb_sr, num.property_srno, num.quantity, num.unit, num.id_no, num.damaged, num.make, num.model_bore, num.license_no, num.licence_issue_by, num.country  from "
                + "t1_registration reg, "
                + "t103_properties pro, "
                + "t1033_numbered num "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and pro.location = reg.location "
                + "and pro.regn_srno = reg.regn_srno "
                + "and num.location = pro.location "
                + "and num.property_srno = pro.property_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0'; ";
//-t312_finalreport-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t312_finalreport 
        selectStringSql[26] = "SELECT fin.location, fin.fr_srno, fin.fr_stat, fin.regn_srno, fin.event_sr1, fin.fr_dt, fin.fr_type, fin.fr_unoccur, fin.fr_courttype, fin.fr_courtname, fin.event_sr2, fin.pprc_no, fin.pp_rcdt, fin.ppdep_date, fin.ppdep_by, fin.pprecd_by, fin.ppname, fin.event_sr3, fin.co_rcno, fin.co_rcdt, fin.co_depdt, fin.co_depby, fin.co_recdby, fin.co_crno, fin.co_crdt, fin.fr_text, fin.fr_doclist, fin.temp_fr_srno, fin.fr_iter, fin.cs_srno, fin.event_sr4, fin.pp_remarks, fin.event_sr5, fin.co_filed, fin.co_remarks, fin.hearing_dt  from  "
                + "t1_registration reg, "
                + "t312_finalreport fin "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and fin.location = reg.location "
                + "and fin.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t312b_fraccused----------------------
//INSERT INTO CIPATemp_DB.dbo.t312b_fraccused 
        selectStringSql[27] = "SELECT fra.location, fra.fr_srno, fra.fr_stat, fra.accused_sr, fra.status_cs, fra.status_accused, fra.temp_fr_srno, fra.fr_iter from  "
                + "t1_registration reg, "
                + "t312_finalreport fin, "
                + "t312b_fraccused fra "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and fin.location = reg.location and fra.fr_iter = fin.fr_iter "
                
                + "and fin.regn_srno = reg.regn_srno "
                + "and fra.location = fin.location "
                + "and fra.fr_srno = fin.fr_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t312c_fractsec----------------------
//INSERT INTO CIPATemp_DB.dbo.t312c_fractsec 
        selectStringSql[28] = "SELECT frac.location, frac.fr_srno, frac.fr_stat, frac.accused_sr, frac.act_code, frac.sec_code, frac.temp_fr_srno, frac.fr_iter  from  "
                + "t1_registration reg, "
                + "t312_finalreport fin, "
                + "t312c_fractsec frac "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and fin.location = reg.location and frac.fr_iter = fin.fr_iter "
                + "and fin.regn_srno = reg.regn_srno "
                + "and frac.location = fin.location "
                + "and frac.fr_srno = fin.fr_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t3034_remandcustody----------------------
//INSERT INTO CIPATemp_DB.dbo.t3034_remandcustody 
        selectStringSql[29] = "SELECT rem.location, rem.arrest_srno, rem.regn_srno, rem.event_sr, rem.court_code, rem.court_name, rem.appear_dt, rem.order_dt, rem.order_type, rem.remand_dt from  "
                + "t1_registration reg, "
                + "t3034_remandcustody rem "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and rem.location = reg.location "
                + "and rem.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t3033_bail----------------------
//INSERT INTO CIPATemp_DB.dbo.t3033_bail 
        selectStringSql[30] = "SELECT ba.location, ba.arrest_srno, ba.regn_srno, ba.event_sr, ba.bail_by, ba.court_code, ba.court_name, ba.appear_dt, ba.order_dt, ba.bail_type, ba.bail_amount1, ba.surity_srno1, ba.bail_amount2, ba.surity_srno2, ba.pr_amount, ba.date_to, ba.accused_sr  from  "
                + "t1_registration reg, "
                + "t3033_bail ba "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and ba.location = reg.location "
                + "and ba.regn_srno = reg.regn_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t406a_courtdisposal----------------------    
//INSERT INTO CIPATemp_DB.dbo.t406a_courtdisposal 
        selectStringSql[31] = "SELECT court.location,court.fr_srno,court.event_sr,court.case_type,court.disposaltype,court.charges_dt,court.judgement_dt,court.ppname,court.reason_acquittal,court.court_stricture,court.appeal_by,court.appeal,court.appeal_court_name,court.appeal_court_place  from  "
                + "t1_registration reg, "
                + "t312_finalreport fin, "
                + "t406a_courtdisposal court "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and fin.location = reg.location "
                + "and fin.regn_srno = reg.regn_srno "
                + "and court.location = fin.location "
                + "and court.fr_srno = fin.fr_srno "
                //                + "and reg.reg_type=1 "
                + "and reg.reg_type_srno <> '0' ";
//-t406b_courtdisposal----------------------    
//INSERT INTO CIPATemp_DB.dbo.t406b_courtdisposal 
        selectStringSql[32] = "SELECT courtb.location,courtb.fr_srno,courtb.arrest_srno,courtb.regn_srno,courtb.convictype,courtb.disposaltype,courtb.acquit_reason,courtb.punishtype,courtb.punishperiod,courtb.fineamt,courtb.bondamt,courtb.bondperiod,courtb.juven_name,courtb.juven_address,courtb.fp_taken  from  "
                + "t1_registration reg, "
                + "t312_finalreport fin, "
                + "t406b_courtdisposal courtb "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and fin.location = reg.location "
                + "and fin.regn_srno = reg.regn_srno "
                + "and courtb.location = fin.location "
                + "and courtb.fr_srno = fin.fr_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t406c_courtdisposal----------------------     
//INSERT INTO CIPATemp_DB.dbo.t406c_courtdisposal 
        selectStringSql[33] = "SELECT courtc.location,courtc.fr_srno,courtc.arrest_srno,courtc.act_code,courtc.sec_code  from  "
                + "t1_registration reg, "
                + "t312_finalreport fin, "
                + "t406c_courtdisposal courtc "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and fin.location = reg.location "
                + "and fin.regn_srno = reg.regn_srno "
                + "and courtc.location = fin.location "
                + "and courtc.fr_srno = fin.fr_srno "
                //                + "and reg.reg_type=1  "
                + "and reg.reg_type_srno <> '0' ";
//-t202_missing----------------------    
//INSERT INTO CIPATemp_DB.dbo.t202_missing 
        selectStringSql[34] = "SELECT mis.location, mis.regn_srno, mis.missing_srno, mis.missing_dt, mis.missing_tm, mis.informant_srno, mis.relation, mis.person_srno, mis.deaf, mis.mind_normal, mis.place_missing, mis.last_seen, mis.cloth_upper, mis.cloth_lower, mis.footwear, mis.others, mis.photograph, mis.gd_entry_no, mis.gd_entry_dt, mis.gd_entry_tm, mis.trace_date, mis.trace_place, mis.trace_by, mis.hoto_name, mis.hoto_parentage, mis.hoto_address, mis.uidb_gdentryno, mis.uidb_gdentry_dt, mis.uidb_gdentry_tm, mis.uidb_match, mis.link_fir "
                + "from t1_registration reg, "
                + "t202_missing mis "
                + " where to_char(reg.reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and mis.location = reg.location "
                + "and mis.regn_srno = reg.regn_srno "
                + "and reg.reg_type='2'  "
                + "and reg.reg_type_srno <> '0' ";
//-t205_unnatural----------------------    
//INSERT INTO CIPATemp_DB.dbo.t205_unnatural 
        selectStringSql[35] = "Select un.location, un.regn_srno, un.ud_srno, un.mlc_srno, un.informant_srno, un.relation, un.found_date, un.found_place, un.postmortem, un.reason_for_no, un.person_srno, un.cloth_upper, un.cloth_lower, un.footwear, un.socks, un.others, un.missing_gdentryno, un.missing_gdentry_dt, un.missing_gdentry_tm, un.missing_match, un.pm_rc_no, un.pm_rc_date, un.pm_thru, un.pm_hospital, un.received_by, un.action, un.action_date, un.action_by, un.recd_name, un.recd_address, un.pm_date, un.pm_report_date, un.receive_date, un.receive_by, un.pm_doctor, un.death_cause, un.mag_rc_no, un.mag_rc_date, un.mag_thru, un.mag_code, un.mag_name, un.mag_recd_by, un.mag_rep_recd_date, un.mag_rep_date, un.mag_remark, un.link_fir, un.recd_relation "
                + "from t205_unnatural un, "
                + "t1_registration reg "
                + " where un.regn_srno = reg.regn_srno "
                + "and un.location = reg.location "
                + "and to_char(reg.reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and reg.reg_type='5' "
                + "and reg.reg_type_srno <> '0' ";
//-t204_mlc----------------------    
//INSERT INTO CIPATemp_DB.dbo.t204_mlc 
        selectStringSql[36] = "SELECT mlc.location, mlc.regn_srno, mlc.mlc_srno, mlc.informant_srno, mlc.relation, mlc.injured_srno, mlc.injury_place, mlc.mlc_no, mlc.mlc_dt, mlc.mlc_tm, mlc.hospital, mlc.injury_dt, mlc.injury_tm, mlc.mlc_receipt, mlc.deposit_date, mlc.deposit_by, mlc.exam_date, mlc.statement_date, mlc.discharge_date, mlc.injury_type, mlc.injury_desc, mlc.ud_srno, mlc.link_fir from "
                + "t1_registration reg, "
                + "t204_mlc mlc "
                + " where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and mlc.location = reg.location "
                + "and mlc.regn_srno = reg.regn_srno "
                + "and reg.reg_type='4'  "
                + "and reg.reg_type_srno <> '0'; ";
//-t207_others----------------------    
//INSERT INTO CIPATemp_DB.dbo.t207_others 
        selectStringSql[37] = "SELECT oth.location,oth.regn_srno,oth.other_srno,oth.complainant_srno,oth.offence_beat,oth.offence_place,oth.offence_dt,oth.offence_tm,oth.offence_brief,oth.property_value,oth.subtype,oth.witness1_sr,oth.witness2_sr,oth.offence_beat_name,oth.offence_village,oth.offence_village_name  from "
                + "t1_registration reg, "
                + "t207_others oth "
                + "where to_char(reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and oth.location = reg.location "
                + "and oth.regn_srno = reg.regn_srno "
                + "and reg.reg_type='9'  "
                + "and reg.reg_type_srno <> '0' ";
        
        
         
        
        
//-t501a_criminal-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t501a_criminal 
        selectStringSql[38] = "SELECT crim.location,crim.crim_srno,crim.dossier_no,crim.dossier_dt,"
                + "crim.scrb_dossier_no,crim.scrb_dossier_dt,crim.persfile_no,crim.persfile_dt,"
                + "crim.histsheet_no,crim.histsheet_dt,crim.rcn,crim.rcn_dt,crim.ncn,crim.ncn_dt,"
                + "crim.po_date,crim.jail_date,crim.name,crim.parent,crim.parentage,crim.sex,"
                + "crim.alias,crim.year_birth,crim.place_birth,crim.marital,crim.nationality,"
                + "crim.passport_no,crim.pass_issue_dt,crim.pass_issue_place,crim.religion,"
                + "crim.category,crim.caste,crim.caste_tribe,crim.living_status,crim.edu_qualif,"
                + "crim.occupation,crim.prof_receiver,crim.income_group,crim.income_source,"
                + "crim.food_habits,crim.diseases,crim.weakness,crim.addiction,crim.advocate_employee,"
                + "crim.interrogation_win,crim.visit_places,crim.history,crim.national_id,crim.photograph,"
                + "crim.fingerprint,crim.height_cm,crim.weight_kg,crim.blood_group,crim.mother_tongue,"
                + "crim.phy_build,crim.phy_complexion,crim.phy_teeth,crim.phy_hair,crim.phy_eyes,"
                + "crim.placeburnmark,crim.placeleucoderma,crim.place_mole,crim.place_scar,"
                + "crim.place_tattoo,crim.tattoo_content,crim.idmark_beard,crim.idmark_face,"
                + "crim.idmark_mostach,crim.idmark_nose,crim.idmark_voice,crim.dresshabit_sex,"
                + "crim.dresshabit_com,crim.walking_style,crim.other_feature,crim.deformities,"
                + "crim.habits,crim.vcnb_arrest,crim.vcnb_convict  from t501a_criminal crim "                  //fix to load data in temp_db begin
                + "where crim_srno in (select distinct(crim_srno) from t5013_cristatus "        
                + " where to_char(reg_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and reg_type_srno <>'0')";    //End
   
//-t5011_criaddress-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5011_criaddress 
        selectStringSql[39] = "SELECT criad.location,criad.crim_srno,criad.addr_sr,criad.addr_type,criad.address,criad.telephone,criad.email,criad.juris_country,criad.juris_dist,criad.juris_ps,criad.juris_beat,criad.add_verified_by,criad.add_verified_dt  from t5011_criaddress criad "
        		+ "where crim_srno in (select distinct(crim_srno) from t5013_cristatus "        
                + " where to_char(reg_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and reg_type_srno <>'0')";    //End
///-t5027_bank-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5027_bank 
        selectStringSql[40] = "SELECT ban.location,ban.crim_srno,ban.account_sr,ban.country,ban.district,ban.bank,ban.branch,ban.city,ban.account_no,ban.locker_no  from t501a_criminal crim, "
                + "t5027_bank ban "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = ban.location "
                + "and crim.crim_srno = ban.crim_srno ";
//-t5012_criknowns-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5012_criknowns 
        selectStringSql[41] = "SELECT crik.location,crik.crim_srno,crik.known_sr,crik.relation,crik.important,crik.kn_type,crik.kn_location,crik.kn_crim_srno,crik.how_associate,crik.name,crik.parent,crik.parentage,crik.sex,crik.year_birth,crik.occupation,crik.juris_country,crik.address,crik.telephone,crik.juris_location,crik.juris_beat,crik.add_verified_by,crik.add_verified_dt  from t501a_criminal crim, "
                + "t5012_criknowns crik "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = crik.location "
                + "and crim.crim_srno = crik.crim_srno ";
//-t5023_operationarea-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5023_operationarea 
        selectStringSql[42] = "SELECT oper.location,oper.crim_srno,oper.area_sr,oper.country_code,oper.state_code,oper.district_code,oper.zip_code,oper.ps_code,oper.town_code  from t501a_criminal crim, "
                + "t5023_operationarea oper "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = oper.location "
                + "and crim.crim_srno = oper.crim_srno ";
//-t5021_general-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5021_general 
        selectStringSql[43] = "SELECT gen.location,gen.crim_srno,gen.terr_category,gen.terr_targets,gen.trained_in_countries,gen.expertise_weapons,gen.expertise_explosive,gen.motivation  from t501a_criminal crim, "
                + "t5021_general gen "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = gen.location "
                + "and crim.crim_srno = gen.crim_srno ";
//-t5022_affiliation-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5022_affiliation 
        selectStringSql[44] = "SELECT aff.location,aff.crim_srno,aff.affl_sr,aff.gang_code,aff.join_dt,aff.recruit_by,aff.recruit_by_alias,aff.belonging_to,aff.status_in_gang  from t501a_criminal crim, "
                + "t5022_affiliation aff "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = aff.location "
                + "and crim.crim_srno = aff.crim_srno ";
//-t5026_employment-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5026_employment 
        selectStringSql[45] = "SELECT emp.location,emp.crim_srno,emp.empl_sr,emp.occupation,emp.from_dt,emp.to_dt,emp.employer,emp.designation,emp.work_place,emp.juris_location  from t501a_criminal crim, "
                + "t5026_employment emp "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = emp.location "
                + "and crim.crim_srno = emp.crim_srno ";
//-t5025_political-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5025_political        
        selectStringSql[46] = "SELECT poli.location,poli.crim_srno,poli.politic_sr,poli.party_name,poli.postition,poli.from_dt,poli.to_dt,poli.area  from t501a_criminal crim, "
                + "t5025_political poli "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = poli.location "
                + "and crim.crim_srno = poli.crim_srno ";
//-t5024_notices----------------------
//INSERT INTO CIPATemp_DB.dbo.t5024_notices 
        selectStringSql[47] = "SELECT noti.location,noti.crim_srno,noti.notice_sr,noti.issue_country,noti.issue_by,noti.reference_no,noti.reference_dt,noti.extradition  from t501a_criminal crim, "
                + "t5024_notices noti "
                + "where to_char(dossier_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                + "and crim.location = noti.location "
                + "and crim.crim_srno = noti.crim_srno ";
//-t503_gang----------------------
//INSERT INTO CIPATemp_DB.dbo.t503_gang 
        selectStringSql[48] = "SELECT gan.location,gan.gang_code,gan.gang_type,gan.activity,gan.goal_aims,gan.targets,gan.strategy,gan.tactics,gan.source_finance,gan.source_recruitment,gan.grading,gan.grading_source,gan.remarks  from t503_gang gan ";
//-t5037_frontalorg----------------------
//INSERT INTO CIPATemp_DB.dbo.t5037_frontalorg 
        selectStringSql[49] = "SELECT fron.location,fron.gang_code,fron.org_sr,fron.org_type,fron.org_name,fron.org_leader,fron.org_strength,fron.org_address,fron.org_charter,fron.org_activity,fron.org_linkages  from t503_gang gan, "
                + "t5037_frontalorg fron "
                + "where fron.location = gan.location "
                + "and fron.gang_code = gan.gang_code ";
//-t5032_support----------------------
//INSERT INTO CIPATemp_DB.dbo.t5032_support 
        selectStringSql[50] = "SELECT sup.location,sup.gang_code,sup.support_sr,sup.country_code,sup.support_orgn_type,sup.support_orgn_name,sup.support_nature  from t503_gang gan, "
                + "t5032_support sup "
                + "where sup.location = gan.location "
                + "and sup.gang_code = gan.gang_code ";
//-t5034_transport----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5034_transport 
        selectStringSql[51] = "SELECT tran.location,tran.gang_code,tran.auto_sr,tran.auto_type,tran.make,tran.regtistration_no,tran.chasis_no,tran.engine_no,tran.owner  from t503_gang gan, "
                + "t5034_transport tran "
                + "where tran.location = gan.location "
                + "and tran.gang_code = gan.gang_code ";
//-t5033_holdings----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5033_holdings 
        selectStringSql[52] = "SELECT hol.location,hol.gang_code,hol.hold_type,hol.hold_type_code,hol.hold_detail,hol.quantity  from t503_gang gan, "
                + "t5033_holdings hol "
                + "where hol.location = gan.location "
                + "and hol.gang_code = gan.gang_code ";
//-t5035_training----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5035_training 
        selectStringSql[53] = "SELECT trai.location,trai.gang_code,trai.train_sr,trai.country_code,trai.state_code,trai.district_code,trai.place,trai.details  from t503_gang gan, "
                + "t5035_training trai "
                + "where trai.location = gan.location "
                + "and trai.gang_code = gan.gang_code ";
//-t5036_hideouts----------------------    
//INSERT INTO CIPATemp_DB.dbo.t5036_hideouts 
        selectStringSql[54] = "SELECT hid.location,hid.gang_code,hid.hide_sr,hid.country_code,hid.state_code,hid.district_code,hid.ps_code,hid.address,hid.owner,hid.occupant  from t503_gang gan, "
                + "t5036_hideouts hid "
                + "where hid.location = gan.location "
                + "and hid.gang_code = gan.gang_code ";
// t099_generaldiary general diary Details------------------
//INSERT INTO CIPATemp_DB.dbo.t099_generaldiary
        selectStringSql[55] = "SELECT location, gd_series, gd_srno, gd_entry_dt, gd_entry_tm, gd_type,entered_by, reporting_officer, reported_by, gd_content FROM t099_generaldiary  "
                + " where to_char(gd_entry_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' ";
// t3_caseprogress Case progress Details------------------
//INSERT INTO CIPATemp_DB.dbo.t3_caseprogress                  
        selectStringSql[56] = "SELECT location,regn_srno,module,event_sr,event_dt,event_code,case_diary_sr, case_diary_dt, freezed, event_desc, case_diary_subno from  t3_caseprogress "
                + " where to_char(event_dt,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' ";
        
//-t304b_seizure-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t304b_seizure 
        selectStringSql[57] = "SELECT seiz.location, seiz.regn_srno, seiz.seizure_srno, seiz.property_seizure_sr, seiz.property_srno, seiz.event_sr1, seiz.deposit_by, seiz.deposit_dt, seiz.deposit_tm, seiz.mal_regyear, seiz.mal_regsr, "
                + "seiz.event_sr2, seiz.match_location, seiz.match_srno, seiz.event_sr3, seiz.released_by, seiz.action_taken, seiz.court_type, seiz.court_name, seiz.location_to, seiz.claimant_srno, seiz.event_sr4, seiz.sent_tofsl, seiz.sent_rcno, seiz.sent_rcdt,"
                + " seiz.sent_thru, seiz.received_by,seiz.event_sr5, seiz.fslreceive_dt, seiz.fslreceived_by, seiz.fsl_refno, seiz.fsl_refdt, seiz.fsl_remark, seiz.event_sr6, seiz.present_by, seiz.present_dt, seiz.present_court_type, "
                + "seiz.present_court_name, seiz.deposit_action_taken, seiz.agency_to,"
                + " seiz.agency_desc, seiz.value, seiz.sent_tofsl_desc, seiz.purpose, seiz.deposit_value FROM "
                + "t1_registration reg, "
                + "t304b_seizure seiz "
//                + "where "
                + " where to_char(reg.reg_date,'YYYYMM') between "
                + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                //                + "and fir.location = reg.location "
                //                + "and fir.regn_srno = reg.regn_srno "
                + "and reg.regn_srno=seiz.regn_srno";
                //                + "and reg.reg_type=1  "
//                + "and reg.reg_type_srno <> '0' ";
        
//-t2012_properties-----------------------    
//INSERT INTO CIPATemp_DB.dbo.t2012_properties 
        selectStringSql[58] = "SELECT prop.location, prop.regn_srno, prop.property_sr, prop.property_srno, prop.event_sr FROM t1_registration reg,"
                      + "t2012_properties prop "
//                      + "where "
                      + " where to_char(reg.reg_date,'YYYYMM') between "
                      + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
                      + "and '" + selectedToYYYY + "" + selectedToMM + "' "
                      //                + "and fir.location = reg.location "
                      //                + "and fir.regn_srno = reg.regn_srno "
                      + "and reg.regn_srno=prop.regn_srno";
                      //                + "and reg.reg_type=1  "
//                      + "and reg.reg_type_srno <> '0' ";
              
//-t015_psstaffolld---------------------- 
//INSERT INTO CIPATemp_DB.dbo.t015_psstaffold              
        selectStringSql[59] = "SELECT location, pis_code, pis_staffname, pis_designation, date_join, place_from, date_relieve, place_to, photograph, pis_role, belt_no, pis_id, pis_passwd, pis_rank, sex, date_birth, parent, parentage, address_perm, address_curr, telephones, emails from t015_psstaffold";
              
//-t011_state---------------------- 
//INSERT INTO CIPATemp_DB.dbo.t011_state              
//        selectStringSql[60] = "SELECT state_code, state_name, short_name from t011_state";

//-t012_district---------------------- 
//INSERT INTO CIPATemp_DB.dbo.t012_district              
//        selectStringSql[61] = "SELECT state_code,district_code, district_name from t012_district";        

//-t013_policestation---------------------- 
//INSERT INTO CIPATemp_DB.dbo.t013_policestation              
//        selectStringSql[62] = "SELECT district_code, ps_code, ps_name from t013_policestation";  
        
//-t5013_cristatus---------------------- 
//INSERT INTO CIPATemp_DB.dbo.t5013_cristatus              
//        selectStringSql[63] = "SELECT cris.location, cris.crim_srno, cris.status_code, cris.status_dt_fr, cris.status_dt_to, cris.arrest_srno, cris.event_code,	cris.reg_location, cris.reg_type, cris.reg_type_srno, cris.reg_dt, cris.mo_sr, cris.refn_no, cris.refn_date, cris.jail_type, cris.jail_name, cris.court_type,	cris.court_name, " 
//        		      + "cris.punish_type, cris.punish_period, cris.fine_amt, cris.bond_amt, cris.bond_period, cris.juven_name, cris.juven_address, cris.remarks, cris.freezed, cris.case_status from t5013_cristatus cris"
//        		      + "where to_char(reg_dt,'YYYYMM') between "
//                      + "'" + selectedFromYYYY + "" + selectedFromMM + "' "
//                      + "and '" + selectedToYYYY + "" + selectedToMM + "' ";
       
        
        return selectStringSql;
    }

    public void setSelections(String[] selectedValues) {
        for (int i = 0; i < selectedValues.length; i++) {
            str[i] = selectedValues[i];
        }
        keyState = str[0];
        keyDistrict = str[1];
        keyPS = str[2];
        selectedState = str[3];
        selectedDistrict = str[4];
        selectedPS = str[5];
        selectedFromMM = str[6];
        selectedFromYYYY = str[7];
        selectedToMM = str[8];
        selectedToYYYY = str[9];
    }
}