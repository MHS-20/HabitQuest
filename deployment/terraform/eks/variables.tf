variable "cluster_version" {
  description = "Kubernetes version for the EKS cluster"
  type        = string
  default     = "1.28"
}

variable "name" {
    type = string
    default = "my-eks-cluster"
}

variable "vpc_id" {
  type = string
}

variable "tags" {
  type = map(string)
}

variable "subnet_ids" {
  type = list(string)
}

variable "node_groups" {
  description = "Managed node group configurations"
  type = map(object({
    desired_capacity = number
    min_capacity     = number
    max_capacity     = number
    instance_types   = list(string)
    key_name         = string
    additional_tags  = map(string)
  }))
  default = {
    managed_nodes = {
      desired_capacity = 2
      min_capacity     = 1
      max_capacity     = 3
      instance_types   = ["t3.small"]
      key_name         = ""
      additional_tags  = {}
    }
  }
}
