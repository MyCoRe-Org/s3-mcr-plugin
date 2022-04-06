import {DerivateTitle} from "@/model/DerivateTitle";

export interface DerivateInfo {
    id:string;
    view:boolean;
    write:boolean;
    delete:boolean;
    titles: DerivateTitle[];
    metadata: Record<string, any>
}